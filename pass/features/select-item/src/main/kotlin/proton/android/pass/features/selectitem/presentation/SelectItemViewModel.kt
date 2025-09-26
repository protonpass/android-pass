/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.selectitem.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByMostRecent
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByTitleDesc
import proton.android.pass.commonui.api.ItemSorter.sortRecentPinTime
import proton.android.pass.commonui.api.ItemSorter.sortSuggestionsByMostRecent
import proton.android.pass.commonui.api.ItemUiFilter.filterByQuery
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.ItemFilterProcessor
import proton.android.pass.data.api.usecases.GetSuggestedAutofillItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.SuggestedAutofillItemsResult
import proton.android.pass.data.api.usecases.passkeys.ObserveItemsWithPasskeys
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareFlag
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.ui.AccountRowUIState
import proton.android.pass.features.selectitem.ui.AccountSwitchUIState
import proton.android.pass.features.selectitem.ui.AutofillItemClickedEvent
import proton.android.pass.features.selectitem.ui.PinningUiState
import proton.android.pass.features.selectitem.ui.SearchInMode
import proton.android.pass.features.selectitem.ui.SearchUiState
import proton.android.pass.features.selectitem.ui.SelectItemListItems
import proton.android.pass.features.selectitem.ui.SelectItemListUiState
import proton.android.pass.features.selectitem.ui.SelectItemSnackbarMessage
import proton.android.pass.features.selectitem.ui.SelectItemUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.searchoptions.api.AutofillSearchOptionsRepository
import proton.android.pass.searchoptions.api.SearchSortingType
import proton.android.pass.searchoptions.api.SortingOption
import javax.inject.Inject

@Suppress("LongParameterList", "LargeClass")
@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getSuggestedAutofillItems: GetSuggestedAutofillItems,
    private val observeItemsWithPasskeys: ObserveItemsWithPasskeys,
    accountManager: AccountManager,
    userManager: UserManager,
    preferenceRepository: UserPreferencesRepository,
    observeItems: ObserveItems,
    observePinnedItems: ObservePinnedItems,
    autofillSearchOptionsRepository: AutofillSearchOptionsRepository,
    observeAutofillShares: ObserveAutofillShares,
    observeUpgradeInfo: ObserveUpgradeInfo,
    getUserPlan: GetUserPlan,
    clock: Clock
) : ViewModel() {

    private val selectItemStateFlow: MutableStateFlow<Option<SelectItemState>> =
        MutableStateFlow(None)
    private val selectedAccountFlow: MutableStateFlow<Option<UserId>> =
        MutableStateFlow(None)

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val debouncedSearchQueryState = searchQueryState
        .debounce(DEBOUNCE_TIMEOUT)
        .onStart { emit("") }
        .distinctUntilChanged()

    private val isInSeeAllPinsModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isProcessingSearchState: MutableStateFlow<IsProcessingSearchState> =
        MutableStateFlow(IsProcessingSearchState.NotLoading)
    private val shouldScrollToTopFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val sortingOptionFlow = autofillSearchOptionsRepository.observeSortingOption()
        .distinctUntilChanged()
        .onEach { shouldScrollToTopFlow.update { true } }

    private val searchWrapper = combine(
        searchQueryState,
        isInSearchModeState,
        isProcessingSearchState,
        ::SearchWrapper
    )

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean,
        val isProcessingSearch: IsProcessingSearchState
    )

    private val usersAutofillSharesMapFlow: Flow<Map<UserId, List<Share>>> =
        accountManager.getAccounts(AccountState.Ready)
            .flatMapLatest { accounts ->
                val flows = accounts.map { account ->
                    observeAutofillShares(account.userId).map { account.userId to it }
                }
                combine(flows) { arrayOfPairs -> arrayOfPairs.toMap() }
            }

    private val shareIdToSharesFlow = usersAutofillSharesMapFlow
        .mapLatest { usersAutofillSharesMap ->
            usersAutofillSharesMap.mapValues { userAutofillSharesEntry ->
                userAutofillSharesEntry.value
                    .associateBy { autofillShare -> autofillShare.id }
                    .toPersistentMap()
            }
        }
        .distinctUntilChanged()

    private val usersAndPlansFlow = accountManager.getAccounts(AccountState.Ready)
        .flatMapLatest { accountList ->
            val flows = accountList.map { account ->
                combine(
                    userManager.observeUser(account.userId).filterNotNull(),
                    getUserPlan(account.userId),
                    ::Pair
                )
            }
            combine(flows) { it.toMap() }
        }

    private val itemUiModelFlow: Flow<LoadingResult<List<ItemUiModel>>> = combine(
        selectItemStateFlow,
        usersAutofillSharesMapFlow,
        selectedAccountFlow,
        usersAndPlansFlow
    ) { selectItemState, usersAutofillShares, selectedAccount, usersAndPlans ->
        when (val state = selectItemState.value()) {
            is SelectItemState.Autofill,
            is SelectItemState.Passkey.Register -> {
                val flows = usersAutofillShares
                    .filter { it.matchesSelectedAccount(selectedAccount) }
                    .filter { (userId, _) ->
                        if (state.itemTypeFilter == ItemTypeFilter.CreditCards) {
                            usersAndPlans.keys.find { it.userId == userId }?.let { user ->
                                usersAndPlans[user]?.let(Plan::hasPlanWithAccess) == true
                            } == true
                        } else {
                            true
                        }
                    }
                    .map { (userId, autofillShares) ->
                        observeItems(
                            userId = userId,
                            filter = state.itemTypeFilter,
                            selection = ShareSelection.Shares(autofillShares.map(Share::id)),
                            itemState = ItemState.Active,
                            shareFlags = mapOf(ShareFlag.IsHidden to false)
                        ).map { autofillShares to it.map(ItemData::DefaultItem) }
                    }
                combine(flows, ItemFilterProcessor::removeDuplicates)
            }

            is SelectItemState.Passkey.Select -> {
                val flows = usersAutofillShares.map { (userId, usableVaults) ->
                    observeItemsWithPasskeys(
                        userId = userId,
                        shareSelection = ShareSelection.Shares(usableVaults.map(Share::id)),
                        includeHiddenVault = false
                    )
                }
                combine(flows) { it.toList().flatten().map(ItemData::DefaultItem) }
            }

            is SelectItemState.Password.Select -> {
                val flows = usersAutofillShares
                    .filter { it.matchesSelectedAccount(selectedAccount) }
                    .map { (userId, autofillShares) ->
                        observeItems(
                            userId = userId,
                            filter = state.itemTypeFilter,
                            selection = ShareSelection.Shares(autofillShares.map(Share::id)),
                            itemState = ItemState.Active,
                            shareFlags = mapOf(ShareFlag.IsHidden to false)
                        )
                            .map { items ->
                                encryptionContextProvider.withEncryptionContext {
                                    items.filter { item ->
                                        (item.itemType as? ItemType.Login)
                                            ?.password
                                            ?.let { encryptedPassword ->
                                                decrypt(encryptedPassword).isNotEmpty()
                                            } == true
                                    }
                                }
                            }
                            .map { autofillShares to it.map(ItemData::DefaultItem) }
                    }
                combine(flows, ItemFilterProcessor::removeDuplicates)
            }

            else -> flowOf(emptyList())
        }
    }
        .flatMapLatest { itemResult ->
            itemResult.map { list ->
                encryptionContextProvider.withEncryptionContext {
                    list.map { suggestedItem ->
                        suggestedItem.item.toUiModel(this@withEncryptionContext)
                    }
                }
            }
        }
        .asLoadingResult()
        .distinctUntilChanged()

    private val sortedListItemFlow: Flow<LoadingResult<List<GroupedItemList>>> = combine(
        itemUiModelFlow,
        sortingOptionFlow
    ) { result, sortingOption ->
        result.map { it.groupedItemLists(sortingOption, clock.now()) }
    }.distinctUntilChanged()

    private val textFilterListItemFlow: Flow<LoadingResult<List<GroupedItemList>>> = combine(
        sortedListItemFlow,
        debouncedSearchQueryState,
        isInSearchModeState
    ) { result, searchQuery, isInSearchMode ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (isInSearchMode && searchQuery.isNotBlank()) {
            result.map { grouped ->
                grouped.map {
                    GroupedItemList(
                        it.key,
                        it.items.filterByQuery(searchQuery)
                    )
                }
            }
        } else {
            result
        }
    }.flowOn(Dispatchers.Default)

    private val pinnedItemsFlow: Flow<LoadingResult<List<Item>>> = combine(
        selectedAccountFlow,
        usersAutofillSharesMapFlow,
        selectItemStateFlow
    ) { selectedAccount, vaultsByUserId, selectItemState: Option<SelectItemState> ->
        when (selectItemState) {
            None -> flowOf(emptyList())
            is Some -> if (selectItemState.value.showPinnedItems) {
                val vaults = if (selectedAccount is Some) {
                    vaultsByUserId.filter { it.key == selectedAccount.value }
                } else {
                    vaultsByUserId
                }
                val flows = vaults.map { (userId, usableVaults) ->
                    observePinnedItems(
                        userId = userId,
                        filter = selectItemState.value.itemTypeFilter,
                        shareSelection = ShareSelection.Shares(usableVaults.map { it.id }),
                        shareFlags = mapOf(ShareFlag.IsHidden to false)
                    )
                }
                combine(flows) { it.toList().flatten() }
            } else {
                // Do not show pinned items
                flowOf(emptyList())
            }
        }
    }.flatMapLatest { it }.asLoadingResult()

    private val pinningUiStateFlow = combine(
        pinnedItemsFlow,
        sortingOptionFlow,
        debouncedSearchQueryState,
        isInSeeAllPinsModeState,
        preferenceRepository.observeDisplayAutofillPinningPreference()
    ) { pinnedItemsResult, sortingOption, searchQuery, isInSeeAllPinsMode, isPinningEnabled ->
        val pinnedItems = pinnedItemsResult.getOrNull()?.let { list ->
            encryptionContextProvider.withEncryptionContext {
                list.map { it.toUiModel(this@withEncryptionContext) }
            }
        } ?: emptyList()
        val unfilteredItems = pinnedItems
            .sortRecentPinTime()
            .toPersistentList()
        val filteredItems = pinnedItems
            .filterByQuery(searchQuery)
            .groupedItemLists(sortingOption, clock.now())
            .toPersistentList()
        PinningUiState(
            inPinningMode = isInSeeAllPinsMode,
            filteredItems = filteredItems,
            unFilteredItems = unfilteredItems,
            showPinning = isPinningEnabled.value
        )
    }

    private val suggestionsItemUIModelFlow: Flow<LoadingResult<List<ItemUiModel>>> =
        selectItemStateFlow
            .flatMapLatest { state ->
                val stateValue = state.value() ?: return@flatMapLatest flowOf(LoadingResult.Loading)
                getSuggestionsForState(stateValue)
            }
            .map { itemResult ->
                itemResult.map { list ->
                    encryptionContextProvider.withEncryptionContext {
                        list.map { suggestedItem ->
                            suggestedItem.item.toUiModel(this@withEncryptionContext)
                        }
                    }
                }
            }

    private val resultsFlow: Flow<LoadingResult<SelectItemListItems>> = combine(
        selectItemStateFlow,
        textFilterListItemFlow,
        suggestionsItemUIModelFlow,
        isInSearchModeState
    ) { selectItemState, result, suggestionsResult, isInSearchMode ->
        result.map { grouped ->
            if (isInSearchMode) {
                SelectItemListItems(
                    suggestions = persistentListOf(),
                    items = grouped.map { GroupedItemList(it.key, it.items) }
                        .filter { it.items.isNotEmpty() }
                        .toImmutableList(),
                    suggestionsForTitle = ""
                )
            } else {
                val suggestions = suggestionsResult.getOrNull() ?: persistentListOf()
                val suggestionIds = suggestions.map { it.id }.toSet()
                SelectItemListItems(
                    suggestions = suggestions
                        .sortSuggestionsByMostRecent()
                        .toImmutableList(),
                    items = grouped
                        .map {
                            it.copy(
                                items = it.items.filter { item -> !suggestionIds.contains(item.id) }
                            )
                        }
                        .filter { it.items.isNotEmpty() }
                        .toImmutableList(),
                    suggestionsForTitle = selectItemState.value()?.suggestionsTitle ?: ""
                )
            }
        }
    }.flowOn(Dispatchers.Default)

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val itemClickedFlow: MutableStateFlow<AutofillItemClickedEvent> =
        MutableStateFlow(AutofillItemClickedEvent.None)

    internal data class AccountsData(
        val accountsMap: Map<User, Plan>,
        val selectedAccount: Option<UserId>
    ) {

        fun displayOnlyPrimaryVaultMessage(allShares: Map<UserId, PersistentMap<ShareId, Share>>): Boolean {
            if (accountsMap.size != 1) return false

            val (user, plan) = accountsMap.entries.first()
            return when (plan.planType) {
                is PlanType.Free -> when (val limit = plan.vaultLimit) {
                    PlanLimit.Unlimited -> false
                    is PlanLimit.Limited -> allShares[user.userId]
                        ?.values
                        ?.filterIsInstance<Share.Vault>()
                        ?.let { vaultShares -> vaultShares.size > limit.limit } == true
                }

                is PlanType.Paid,
                is PlanType.Trial,
                is PlanType.Unknown -> false
            }
        }

        fun toAccountSwitchUIState(): AccountSwitchUIState = AccountSwitchUIState(
            selectedAccount,
            accountsMap.map { AccountRowUIState(it.key.userId, it.key.email.orEmpty()) }
        )
    }

    private val accountsDataFlow = combine(
        usersAndPlansFlow,
        selectedAccountFlow,
        SelectItemViewModel::AccountsData
    )

    private val selectItemListUiStateFlow = combineN(
        accountsDataFlow,
        resultsFlow,
        isRefreshing,
        itemClickedFlow,
        sortingOptionFlow,
        shareIdToSharesFlow,
        shouldScrollToTopFlow,
        preferenceRepository.getUseFaviconsPreference(),
        observeUpgradeInfo().asLoadingResult(),
        selectItemStateFlow
    ) { accountData,
        itemsResult,
        isRefreshing,
        itemClicked,
        sortingSelection,
        shares,
        shouldScrollToTop,
        useFavicons,
        upgradeInfo,
        selectItemState ->
        val isLoading = IsLoadingState.from(itemsResult is LoadingResult.Loading)
        val items = when (itemsResult) {
            LoadingResult.Loading -> SelectItemListItems.Initial
            is LoadingResult.Success -> itemsResult.data
            is LoadingResult.Error -> {
                PassLogger.i(
                    TAG,
                    itemsResult.exception,
                    "Could not load autofill items"
                )
                snackbarDispatcher(SelectItemSnackbarMessage.LoadItemsError)
                SelectItemListItems.Initial
            }
        }

        val canUpgrade = upgradeInfo.getOrNull()?.isUpgradeAvailable == true
        val showCreateButton = selectItemState.map { it.showCreateButton }.value() == true
        val isPasswordCredential = selectItemState.map { it.isPasswordCredentialCreation }.value() == true

        SelectItemListUiState(
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            itemClickedEvent = itemClicked,
            items = items,
            shares = shares.values.reduce { acc, persistentMap -> acc.putAll(persistentMap) },
            sortingType = sortingSelection.searchSortingType,
            shouldScrollToTop = shouldScrollToTop,
            canLoadExternalImages = useFavicons.value(),
            displayOnlyPrimaryVaultMessage = accountData.displayOnlyPrimaryVaultMessage(shares),
            canUpgrade = canUpgrade,
            displayCreateButton = showCreateButton,
            accountSwitchState = accountData.toAccountSwitchUIState(),
            isPasswordCredentialCreation = isPasswordCredential
        )
    }

    internal val uiState: StateFlow<SelectItemUiState> = combine(
        selectItemListUiStateFlow,
        searchWrapper,
        pinningUiStateFlow,
        getUserPlan().asLoadingResult(),
        shareIdToSharesFlow
    ) { selectItemListUiState,
        search,
        pinningUiState,
        planRes,
        shares ->
        val searchIn = planRes.getOrNull()?.run {
            when (planType) {
                is PlanType.Free -> {
                    when (val limit = vaultLimit) {
                        PlanLimit.Unlimited -> SearchInMode.AllVaults
                        is PlanLimit.Limited -> if (shares.size > limit.limit) {
                            SearchInMode.OldestVaults
                        } else {
                            SearchInMode.AllVaults
                        }
                    }
                }

                else -> SearchInMode.AllVaults
            }
        } ?: SearchInMode.OldestVaults

        SelectItemUiState(
            listUiState = selectItemListUiState,
            searchUiState = SearchUiState(
                searchQuery = search.searchQuery,
                inSearchMode = search.isInSearchMode,
                isProcessingSearch = search.isProcessingSearch,
                searchInMode = searchIn
            ),
            pinningUiState = pinningUiState
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    internal fun onItemClicked(item: ItemUiModel) {
        itemClickedFlow.update { AutofillItemClickedEvent.ItemClicked(item) }
    }

    internal fun onSuggestionClicked(item: ItemUiModel) {
        itemClickedFlow.update { AutofillItemClickedEvent.SuggestionClicked(item) }
    }

    internal fun onSearchQueryChange(query: String) {
        if (query.contains("\n")) return

        searchQueryState.update { query }
        isProcessingSearchState.update { IsProcessingSearchState.Loading }
    }

    internal fun onStopSearching() {
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
    }

    internal fun onEnterSearch() {
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
    }

    internal fun setInitialState(state: SelectItemState) {
        selectItemStateFlow.update { state.toOption() }
    }

    internal fun onScrolledToTop() {
        shouldScrollToTopFlow.update { false }
    }

    internal fun onEnterSeeAllPinsMode() {
        isInSeeAllPinsModeState.update { true }
    }

    internal fun onStopPinningMode() {
        isInSeeAllPinsModeState.update { false }
    }

    internal fun clearEvent() {
        itemClickedFlow.update { AutofillItemClickedEvent.None }
    }

    private fun List<ItemUiModel>.groupedItemLists(sortingOption: SortingOption, instant: Instant) =
        when (sortingOption.searchSortingType) {
            SearchSortingType.MostRecent -> groupAndSortByMostRecent(instant)
            SearchSortingType.TitleAsc -> groupAndSortByTitleAsc()
            SearchSortingType.TitleDesc -> groupAndSortByTitleDesc()
            SearchSortingType.CreationAsc -> groupAndSortByCreationAsc()
            SearchSortingType.CreationDesc -> groupAndSortByCreationDesc()
        }

    private fun getSuggestionsForState(state: SelectItemState) = when (state) {
        is SelectItemState.Autofill -> getSuggestionsForAutofill(state)
        is SelectItemState.Passkey -> getSuggestionsForPasskey(state)
        is SelectItemState.Password -> getSuggestionsForPassword(state)
    }

    private fun getSuggestionsForAutofill(state: SelectItemState.Autofill) = when (state) {
        is SelectItemState.Autofill.Login -> {
            selectedAccountFlow.flatMapLatest { userId ->
                getSuggestedAutofillItems(
                    itemTypeFilter = ItemTypeFilter.Logins,
                    suggestion = state.suggestion,
                    userId = userId
                )
            }
                .filterIsInstance<SuggestedAutofillItemsResult.Items>()
                .map { list -> list.suggestedItems }
                .asResultWithoutLoading()
        }

        is SelectItemState.Autofill.CreditCard -> flowOf(LoadingResult.Success(emptyList()))
        is SelectItemState.Autofill.Identity -> flowOf(LoadingResult.Success(emptyList()))
    }

    private fun getSuggestionsForPasskey(state: SelectItemState.Passkey) = when (state) {
        is SelectItemState.Passkey.Register -> {
            selectedAccountFlow.flatMapLatest { userId ->
                getSuggestedAutofillItems(
                    itemTypeFilter = ItemTypeFilter.Logins,
                    suggestion = state.suggestion,
                    userId = userId
                )
            }
                .filterIsInstance<SuggestedAutofillItemsResult.Items>()
                .map { list -> list.suggestedItems }
                .asResultWithoutLoading()
        }

        // TBD: Implement getSuggestionsForPasskey
        is SelectItemState.Passkey.Select -> flowOf(LoadingResult.Success(emptyList()))
    }

    private fun getSuggestionsForPassword(state: SelectItemState.Password) = when (state) {
        is SelectItemState.Password.Register -> flowOf(LoadingResult.Success(emptyList()))

        is SelectItemState.Password.Select -> selectedAccountFlow.flatMapLatest { userId ->
            getSuggestedAutofillItems(
                itemTypeFilter = ItemTypeFilter.Logins,
                suggestion = state.suggestion,
                userId = userId
            )
        }
            .filterIsInstance<SuggestedAutofillItemsResult.Items>()
            .map { list -> list.suggestedItems }
            .asResultWithoutLoading()
    }

    internal fun onAccountSwitch(userId: Option<UserId>) {
        selectedAccountFlow.update { userId }
    }

    private fun <K, V> Map.Entry<K, V>.matchesSelectedAccount(selectedAccount: Option<UserId>): Boolean =
        if (selectedAccount is Some) {
            this.key == selectedAccount.value
        } else {
            true
        }

    private companion object {

        private const val DEBOUNCE_TIMEOUT = 300L

        private const val TAG = "SelectItemViewModel"

    }

}

/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill.ui.autofill.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.AutofillDisplayed
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.MFAAutofillCopied
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.heuristics.ItemFieldMapper
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.common.AutofillConfirmMode
import proton.android.pass.autofill.ui.autofill.select.SelectItemSnackbarMessage.LoadItemsError
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByMostRecent
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.groupAndSortByTitleDesc
import proton.android.pass.commonui.api.ItemSorter.sortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.sortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.sortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.sortByTitleDesc
import proton.android.pass.commonui.api.ItemSorter.sortMostRecent
import proton.android.pass.commonui.api.ItemSorter.sortSuggestionsByMostRecent
import proton.android.pass.commonui.api.ItemUiFilter.filterByQuery
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.featuresearchoptions.api.AutofillSearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import java.net.URI
import javax.inject.Inject

@Suppress("LongParameterList", "LargeClass")
@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val updateAutofillItem: UpdateAutofillItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val toastManager: ToastManager,
    private val preferenceRepository: UserPreferencesRepository,
    private val telemetryManager: TelemetryManager,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    observeActiveItems: ObserveActiveItems,
    observePinnedItems: ObservePinnedItems,
    getSuggestedLoginItems: GetSuggestedLoginItems,
    observeVaults: ObserveVaults,
    autofillSearchOptionsRepository: AutofillSearchOptionsRepository,
    getUserPlan: GetUserPlan,
    observeUpgradeInfo: ObserveUpgradeInfo,
    clock: Clock
) : ViewModel() {

    private val autofillAppStateFlow: MutableStateFlow<Option<AutofillAppState>> =
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

    private val planFlow = getUserPlan()
        .asLoadingResult()
        .distinctUntilChanged()

    private val vaultsFlow = observeVaults().asLoadingResult()

    private val shareIdToSharesFlow = vaultsFlow
        .map { vaultsResult ->
            val vaults = vaultsResult.getOrNull() ?: emptyList()
            vaults.associate { it.shareId to ShareUiModel.fromVault(it) }
                .toPersistentMap()
        }
        .distinctUntilChanged()

    private val itemFiltersFlow = combine(
        planFlow,
        vaultsFlow,
        autofillAppStateFlow
    ) { planRes, vaultsRes, appStateOption ->
        val vaults = when (vaultsRes) {
            is LoadingResult.Success -> vaultsRes.data
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error observing vaults")
                PassLogger.w(TAG, vaultsRes.exception)
                return@combine LoadingResult.Error(vaultsRes.exception)
            }

            LoadingResult.Loading -> return@combine LoadingResult.Loading
        }
        val plan = when (planRes) {
            is LoadingResult.Success -> planRes.data
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error observing plan")
                PassLogger.w(TAG, planRes.exception)
                return@combine LoadingResult.Error(planRes.exception)
            }

            LoadingResult.Loading -> return@combine LoadingResult.Loading
        }
        val shareSelection = getShareSelection(plan.planType, vaults)
        val state = appStateOption.value() ?: return@combine LoadingResult.Loading
        val itemTypeFilter: ItemTypeFilter = when (state.autofillData.assistInfo.cluster) {
            is NodeCluster.CreditCard -> ItemTypeFilter.CreditCards
            is NodeCluster.Login,
            is NodeCluster.SignUp -> ItemTypeFilter.Logins

            else -> return@combine LoadingResult.Error(IllegalStateException("Unknown cluster type"))
        }
        LoadingResult.Success(itemTypeFilter to shareSelection)
    }

    private val itemUiModelFlow: Flow<LoadingResult<List<ItemUiModel>>> = itemFiltersFlow
        .flatMapLatest {
            val (filter, selection) = when (it) {
                is LoadingResult.Error -> {
                    PassLogger.w(TAG, "Error observing plan")
                    PassLogger.w(TAG, it.exception)
                    return@flatMapLatest flowOf(LoadingResult.Error(it.exception))
                }

                LoadingResult.Loading -> return@flatMapLatest flowOf(LoadingResult.Loading)
                is LoadingResult.Success -> it.data
            }
            observeActiveItems(
                filter = filter,
                shareSelection = selection
            ).asResultWithoutLoading()
        }
        .map { itemResult ->
            itemResult.map { list ->
                encryptionContextProvider.withEncryptionContext {
                    list.map { it.toUiModel(this@withEncryptionContext) }
                }
            }
        }
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

    private val pinnedItemsFlow = itemFiltersFlow
        .flatMapLatest {
            val (filter, shareSelection) = when (it) {
                is LoadingResult.Error -> return@flatMapLatest flowOf(LoadingResult.Error(it.exception))
                LoadingResult.Loading -> return@flatMapLatest flowOf(LoadingResult.Loading)
                is LoadingResult.Success -> it.data
            }
            observePinnedItems(filter = filter, shareSelection = shareSelection).asLoadingResult()
        }

    private val pinningUiStateFlow = combine(
        pinnedItemsFlow,
        sortingOptionFlow,
        debouncedSearchQueryState,
        isInSeeAllPinsModeState,
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PINNING_V1)
    ) { pinnedItemsResult, sortingOption, searchQuery, isInSeeAllPinsMode, isPinningEnabled ->
        val pinnedItems = if (isPinningEnabled) {
            pinnedItemsResult.getOrNull()?.let { list ->
                encryptionContextProvider.withEncryptionContext {
                    list.map { it.toUiModel(this@withEncryptionContext) }
                }
            } ?: emptyList()
        } else {
            emptyList()
        }
        val unfilteredItems = pinnedItems
            .sortItemLists(sortingOption)
            .toPersistentList()
        val filteredItems = pinnedItems
            .filterByQuery(searchQuery)
            .groupedItemLists(sortingOption, clock.now())
            .toPersistentList()
        PinningUiState(
            inPinningMode = isInSeeAllPinsMode,
            isPinningEnabled = isPinningEnabled,
            filteredItems = filteredItems,
            unFilteredItems = unfilteredItems,
        )
    }

    private val suggestionsItemUIModelFlow: Flow<LoadingResult<List<ItemUiModel>>> =
        autofillAppStateFlow
            .flatMapLatest { state ->
                if (state is Some) {
                    val autofillData = state.value.autofillData
                    when (autofillData.assistInfo.cluster) {
                        is NodeCluster.CreditCard -> flowOf(LoadingResult.Success(emptyList()))
                        is NodeCluster.Login,
                        is NodeCluster.SignUp -> {
                            val packageName = autofillData.packageInfo.packageName
                                .takeIf { !it.isBrowser() }
                                .toOption()
                                .map { it.value }

                            getSuggestedLoginItems(
                                packageName = packageName,
                                url = autofillData.assistInfo.url
                            ).asResultWithoutLoading()
                        }

                        else -> flowOf(LoadingResult.Success(emptyList()))
                    }
                } else {
                    flowOf(LoadingResult.Loading)
                }
            }
            .map { itemResult ->
                itemResult.map { list ->
                    encryptionContextProvider.withEncryptionContext {
                        list.map { it.toUiModel(this@withEncryptionContext) }
                    }
                }
            }

    private val resultsFlow: Flow<LoadingResult<SelectItemListItems>> = combine(
        autofillAppStateFlow,
        textFilterListItemFlow,
        suggestionsItemUIModelFlow,
        isInSearchModeState
    ) { autofillAppState, result, suggestionsResult, isInSearchMode ->
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
                    suggestionsForTitle = autofillAppState.value()
                        ?.let(::getSuggestionsTitle)
                        ?: ""
                )
            }
        }
    }.flowOn(Dispatchers.Default)

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val itemClickedFlow: MutableStateFlow<AutofillItemClickedEvent> =
        MutableStateFlow(AutofillItemClickedEvent.None)

    private val selectItemListUiStateFlow = combineN(
        resultsFlow,
        isRefreshing,
        itemClickedFlow,
        sortingOptionFlow,
        shareIdToSharesFlow,
        shouldScrollToTopFlow,
        preferenceRepository.getUseFaviconsPreference(),
        planFlow,
        observeUpgradeInfo().asLoadingResult(),
    ) { itemsResult,
        isRefreshing,
        itemClicked,
        sortingSelection,
        shares,
        shouldScrollToTop,
        useFavicons,
        planRes,
        upgradeInfo ->
        val isLoading =
            IsLoadingState.from(itemsResult is LoadingResult.Loading || planRes is LoadingResult.Loading)
        val items = when (itemsResult) {
            LoadingResult.Loading -> SelectItemListItems.Initial
            is LoadingResult.Success -> itemsResult.data
            is LoadingResult.Error -> {
                PassLogger.i(
                    TAG,
                    itemsResult.exception,
                    "Could not load autofill items"
                )
                snackbarDispatcher(LoadItemsError)
                SelectItemListItems.Initial
            }
        }

        val displayOnlyPrimaryVaultMessage = planRes.getOrNull()?.run {
            when (planType) {
                is PlanType.Free -> when (val limit = vaultLimit) {
                    PlanLimit.Unlimited -> false
                    is PlanLimit.Limited -> shares.size > limit.limit
                }

                else -> false
            }
        } ?: false

        val canUpgrade = upgradeInfo.getOrNull()?.isUpgradeAvailable ?: false

        SelectItemListUiState(
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            itemClickedEvent = itemClicked,
            items = items,
            shares = shares,
            sortingType = sortingSelection.searchSortingType,
            shouldScrollToTop = shouldScrollToTop,
            canLoadExternalImages = useFavicons.value(),
            displayOnlyPrimaryVaultMessage = displayOnlyPrimaryVaultMessage,
            canUpgrade = canUpgrade
        )
    }

    val uiState: StateFlow<SelectItemUiState> = combineN(
        selectItemListUiStateFlow,
        searchWrapper,
        pinningUiStateFlow,
        planFlow,
        autofillAppStateFlow,
        shareIdToSharesFlow,
    ) { selectItemListUiState,
        search,
        pinningUiState,
        planRes,
        appState,
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
            pinningUiState = pinningUiState,
            confirmMode = appState.flatMap { state ->
                if (state.autofillData.isDangerousAutofill) {
                    AutofillConfirmMode.DangerousAutofill.some()
                } else {
                    None
                }
            }
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(
        item: ItemUiModel,
        autofillAppState: AutofillAppState,
        shouldAssociate: Boolean
    ) {
        when (val autofillItem = item.toAutoFillItem()) {
            is AutofillItem.Login -> onLoginItemClicked(
                autofillItem = autofillItem,
                autofillAppState = autofillAppState,
                shouldAssociate = shouldAssociate,
            )

            is AutofillItem.CreditCard -> onCreditCardClicked(
                autofillItem = autofillItem,
                autofillAppState = autofillAppState,
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        if (query.contains("\n")) return

        searchQueryState.update { query }
        isProcessingSearchState.update { IsProcessingSearchState.Loading }
    }

    fun onStopSearching() {
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
    }

    fun onEnterSearch() {
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
    }

    fun setInitialState(autofillAppState: AutofillAppState) {
        autofillAppStateFlow.update { autofillAppState.toOption() }

        val event = AutofillDisplayed(
            source = AutofillTriggerSource.App,
            app = autofillAppState.autofillData.packageInfo.packageName
        )
        telemetryManager.sendEvent(event)
    }

    fun onScrolledToTop() {
        shouldScrollToTopFlow.update { false }
    }

    fun onEnterSeeAllPinsMode() {
        isInSeeAllPinsModeState.update { true }
    }

    fun onStopPinningMode() {
        isInSeeAllPinsModeState.update { false }
    }

    private fun onLoginItemClicked(
        autofillItem: AutofillItem.Login,
        autofillAppState: AutofillAppState,
        shouldAssociate: Boolean
    ) = encryptionContextProvider.withEncryptionContext {
        handleTotpUri(this@withEncryptionContext, autofillItem.totp)

        val (updatePackageInfo, updateUrl) = autofillAppState.updateAutofillFields()
        updateAutofillItem(
            UpdateAutofillItemData(
                shareId = ShareId(autofillItem.shareId),
                itemId = ItemId(autofillItem.itemId),
                packageInfo = updatePackageInfo,
                url = updateUrl,
                shouldAssociate = shouldAssociate
            )
        )

        val mappings = ItemFieldMapper.mapFields(
            encryptionContext = this@withEncryptionContext,
            autofillItem = autofillItem,
            cluster = autofillAppState.autofillData.assistInfo.cluster
        )
        itemClickedFlow.update {
            AutofillItemClickedEvent.Clicked(mappings)
        }
    }

    private fun onCreditCardClicked(
        autofillItem: AutofillItem.CreditCard,
        autofillAppState: AutofillAppState
    ) = encryptionContextProvider.withEncryptionContext {

        val (updatePackageInfo, updateUrl) = autofillAppState.updateAutofillFields()
        updateAutofillItem(
            UpdateAutofillItemData(
                shareId = ShareId(autofillItem.shareId),
                itemId = ItemId(autofillItem.itemId),
                packageInfo = updatePackageInfo,
                url = updateUrl,
                shouldAssociate = false
            )
        )

        val mappings = ItemFieldMapper.mapFields(
            encryptionContext = this@withEncryptionContext,
            autofillItem = autofillItem,
            cluster = autofillAppState.autofillData.assistInfo.cluster
        )
        itemClickedFlow.update {
            AutofillItemClickedEvent.Clicked(mappings)
        }
    }

    private fun getSuggestionsTitle(autofillAppState: AutofillAppState): String =
        if (autofillAppState.autofillData.assistInfo.url is Some) {
            getSuggestionsTitleForDomain(autofillAppState.autofillData.assistInfo.url.value)
        } else {
            autofillAppState.autofillData.packageInfo.appName.value
        }

    private fun getSuggestionsTitleForDomain(domain: String): String =
        UrlSanitizer.sanitize(domain).fold(
            onSuccess = {
                runCatching {
                    val parsed = URI(it)
                    parsed.host
                }.getOrDefault("")
            },
            onFailure = {
                PassLogger.i(TAG, it, "Error sanitizing URL [url=$domain]")
                ""
            }
        )

    private fun handleTotpUri(encryptionContext: EncryptionContext, totp: EncryptedString?) {
        if (totp == null) return

        val totpUri = encryptionContext.decrypt(totp)
        val copyTotpToClipboard = runBlocking {
            preferenceRepository.getCopyTotpToClipboardEnabled().first()
        }
        if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
            viewModelScope.launch {
                getTotpCodeFromUri(totpUri)
                    .onSuccess {
                        clipboardManager.copyToClipboard(it)
                        telemetryManager.sendEvent(MFAAutofillCopied)
                        toastManager.showToast(R.string.autofill_notification_copy_to_clipboard)
                    }
                    .onFailure {
                        PassLogger.w(TAG, "Could not copy totp code")
                    }
            }
        }
    }

    private fun getShareSelection(
        planType: PlanType,
        vaults: List<Vault>,
    ): ShareSelection = when (planType) {
        is PlanType.Paid,
        is PlanType.Trial -> ShareSelection.AllShares

        is PlanType.Free,
        is PlanType.Unknown -> vaults
            .filter { vault -> vault.role.toPermissions().canCreate() }
            .map { writeableVault -> writeableVault.shareId }
            .let { writeableVaults -> ShareSelection.Shares(writeableVaults) }
    }

    private fun List<ItemUiModel>.sortItemLists(sortingOption: SortingOption) =
        when (sortingOption.searchSortingType) {
            SearchSortingType.MostRecent -> sortMostRecent()
            SearchSortingType.TitleAsc -> sortByTitleAsc()
            SearchSortingType.TitleDesc -> sortByTitleDesc()
            SearchSortingType.CreationAsc -> sortByCreationAsc()
            SearchSortingType.CreationDesc -> sortByCreationDesc()
        }

    private fun List<ItemUiModel>.groupedItemLists(
        sortingOption: SortingOption,
        instant: Instant
    ) = when (sortingOption.searchSortingType) {
        SearchSortingType.MostRecent -> groupAndSortByMostRecent(instant)
        SearchSortingType.TitleAsc -> groupAndSortByTitleAsc()
        SearchSortingType.TitleDesc -> groupAndSortByTitleDesc()
        SearchSortingType.CreationAsc -> groupAndSortByCreationAsc()
        SearchSortingType.CreationDesc -> groupAndSortByCreationDesc()
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "SelectItemViewModel"
    }
}

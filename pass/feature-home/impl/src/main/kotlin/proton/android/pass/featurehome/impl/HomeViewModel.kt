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

package proton.android.pass.featurehome.impl

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.isReady
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.AppUrls
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys.NoGrouping
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
import proton.android.pass.commonui.api.ItemUiFilter.filterByQuery
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.AliasItemsChangeStatusResult
import proton.android.pass.data.api.repositories.BulkMoveToVaultEvent
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.PinItemsResult
import proton.android.pass.data.api.usecases.ChangeAliasStatus
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveAppNeedsUpdate
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObservePinnedItems
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.PinItems
import proton.android.pass.data.api.usecases.RestoreAllItems
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.UnpinItems
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry.SearchEntrySelection
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import proton.android.pass.domain.canUpdate
import proton.android.pass.domain.toPermissions
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasItemsDisabledError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasItemsEnabledError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasItemsEnabledPartialSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasItemsEnabledSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ClearTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.CreditCardMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemsSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.IdentityMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsMovedToTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsMovedToTrashSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsPinnedError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsPinnedPartialSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsPinnedSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsUnpinnedError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsUnpinnedPartialSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsUnpinnedSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.LoginMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.MoveToTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ObserveItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RefreshError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RestoreItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RestoreItemsSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.searchoptions.api.FilterOption
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.SearchOptions
import proton.android.pass.searchoptions.api.SearchSortingType
import proton.android.pass.searchoptions.api.SortingOption
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList", "LargeClass", "TooManyFunctions")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trashItems: TrashItems,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val performSync: PerformSync,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val restoreItems: RestoreItems,
    private val restoreAllItems: RestoreAllItems,
    private val deleteItem: DeleteItems,
    private val clearTrash: ClearTrash,
    private val addSearchEntry: AddSearchEntry,
    private val deleteSearchEntry: DeleteSearchEntry,
    private val deleteAllSearchEntry: DeleteAllSearchEntry,
    private val observeSearchEntry: ObserveSearchEntry,
    private val telemetryManager: TelemetryManager,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val toastManager: ToastManager,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val pinItems: PinItems,
    private val unpinItems: UnpinItems,
    private val changeAliasStatus: ChangeAliasStatus,
    private val observeCurrentUser: ObserveCurrentUser,
    observeVaults: ObserveVaults,
    clock: Clock,
    observeEncryptedItems: ObserveEncryptedItems,
    observePinnedItems: ObservePinnedItems,
    preferencesRepository: UserPreferencesRepository,
    observeAppNeedsUpdate: ObserveAppNeedsUpdate,
    appDispatchers: AppDispatchers,
    getUserPlan: GetUserPlan,
    savedState: SavedStateHandleProvider,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    observeItemCount: ObserveItemCount,
    accountManager: AccountManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            val isExtraLoggingEnabled =
                featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.EXTRA_LOGGING)
                    .firstOrNull()
                    ?: false
            if (isExtraLoggingEnabled) {
                accountManager.getPrimaryAccount()
                    .distinctUntilChanged()
                    .flatMapLatest { account ->
                        observeItemCount()
                            .map { it.total }
                            .distinctUntilChanged()
                            .scan(initial = null to 0L) { previousPair: Pair<Long?, Long>, current: Long ->
                                previousPair.second to current
                            }
                            .filter { (previous, _) -> previous != null }
                            .map { account to it }
                    }
                    .map { (account, data): Pair<Account?, Pair<Long?, Long>> ->
                        val (previous, current) = data
                        val isAccountReady = account?.isReady() ?: false
                        val itemsDisappeared =
                            previous != null && previous > ITEM_DELETED_THRESHOLD && current == 0L
                        if (isAccountReady && itemsDisappeared) {
                            PassLogger.e(TAG, "All items have been deleted!")
                        }
                    }
                    .launchIn(this)
            }
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    // Variable to keep track of whether the user has entered the search in this session, so we
    // don't send an EnterSearch event every time they click on the search bar
    private var hasEnteredSearch = false

    private val shouldScrollToTopFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isInSuggestionsModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isProcessingSearchState: MutableStateFlow<IsProcessingSearchState> =
        MutableStateFlow(IsProcessingSearchState.NotLoading)
    private val selectionState: MutableStateFlow<SelectionState> =
        MutableStateFlow(SelectionState.Initial)
    private val isInSeeAllPinsModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val navEventState: MutableStateFlow<HomeNavEvent> =
        MutableStateFlow(HomeNavEvent.Unknown)

    @OptIn(FlowPreview::class)
    private val debouncedSearchQueryState = searchQueryState
        .debounce(DEBOUNCE_TIMEOUT)
        .onStart { emit("") }
        .distinctUntilChanged()

    private val searchOptionsFlow = homeSearchOptionsRepository
        .observeSearchOptions()
        .distinctUntilChanged()
        .onEach { shouldScrollToTopFlow.update { true } }

    private val shareListWrapperFlow: Flow<ShareListWrapper> = combine(
        searchOptionsFlow,
        observeVaults().asLoadingResult()
    ) { searchOptions: SearchOptions, vaultsResult ->
        val vaults: List<Vault> = vaultsResult.getOrNull().orEmpty()
        val selectedShare: Option<ShareUiModel> = searchOptions.vaultSelectionOption
            .let { it as? VaultSelectionOption.Vault }
            ?.let { vault -> vaults.firstOrNull { it.shareId == vault.shareId } }
            ?.let(ShareUiModel.Companion::fromVault)
            .toOption()

        val isVaultSelected = searchOptions.vaultSelectionOption is VaultSelectionOption.Vault
        if (isVaultSelected) {
            autoSelectAllVaultsIfCannotFindVault(selectedShare, vaults, searchOptions.userId)
        }
        autoSelectVaultIfSingle(vaults, searchOptions.vaultSelectionOption, searchOptions.userId)

        ShareListWrapper(
            shares = vaults.associate { it.shareId to ShareUiModel.fromVault(it) }
                .toPersistentMap(),
            selectedShare = selectedShare
        )
    }.distinctUntilChanged()

    data class ShareListWrapper(
        val shares: ImmutableMap<ShareId, ShareUiModel>,
        val selectedShare: Option<ShareUiModel>
    ) {
        companion object {
            val Empty = ShareListWrapper(persistentMapOf(), None)
        }
    }

    private val searchEntryState: StateFlow<List<SearchEntry>> =
        searchOptionsFlow.map { it.vaultSelectionOption }
            .flatMapLatest {
                when (val vaultSelection = it) {
                    VaultSelectionOption.AllVaults ->
                        observeSearchEntry(SearchEntrySelection.AllVaults)

                    VaultSelectionOption.Trash -> emptyFlow()
                    is VaultSelectionOption.Vault ->
                        observeSearchEntry(SearchEntrySelection.Vault(vaultSelection.shareId))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    private data class ActionRefreshingWrapper(
        val refreshing: IsRefreshingState,
        val actionState: ActionState
    )

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    private val actionStateFlow: MutableStateFlow<ActionState> =
        MutableStateFlow(ActionState.Unknown)

    private val itemUiModelFlow = searchOptionsFlow.map { it.vaultSelectionOption }
        .flatMapLatest { vault ->
            val (shareSelection, itemState) = when (vault) {
                VaultSelectionOption.AllVaults -> ShareSelection.AllShares to ItemState.Active
                is VaultSelectionOption.Vault -> ShareSelection.Share(vault.shareId) to ItemState.Active
                VaultSelectionOption.Trash -> ShareSelection.AllShares to ItemState.Trashed
            }

            observeEncryptedItems(
                selection = shareSelection,
                itemState = itemState,

                // We observe them all, because otherwise in the All part of the search we would not
                // know how many ItemTypes are there for the other ItemTypes.
                // We filter out the results using the filterByType function
                filter = ItemTypeFilter.All
            ).asResultWithoutLoading()
                .map { itemResult ->
                    itemResult.map { list ->
                        encryptionContextProvider.withEncryptionContextSuspendable {
                            list.asSequence()
                                .map { item -> item.toUiModel(this@withEncryptionContextSuspendable) }
                                .toList()
                        }
                    }
                }
        }
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val filteredSearchEntriesFlow = combine(
        itemUiModelFlow.onEach {
            PassLogger.i(TAG, "Item list size: ${it.getOrNull()?.size}")
        },
        searchEntryState
    ) { itemResult, searchEntryList ->
        itemResult.map { list ->
            val searchEntryMap = searchEntryList.associateBy { it.itemId to it.shareId }
            list.filter { searchEntryMap.containsKey(it.id to it.shareId) }
                .sortedByDescending { searchEntryMap[it.id to it.shareId]?.createTime }
                .takeIf { it.isNotEmpty() }
                ?.let { persistentListOf(GroupedItemList(NoGrouping, it)) }
                ?: persistentListOf()
        }
    }.distinctUntilChanged()

    private val sortedListItemFlow = combine(
        itemUiModelFlow,
        searchOptionsFlow.map { it.sortingOption }
    ) { result, sortingOption ->
        result.map { it.groupedItemLists(sortingOption, clock.now()) }
    }.distinctUntilChanged()

    private val textFilterListItemFlow = combine(
        sortedListItemFlow,
        debouncedSearchQueryState,
        isInSearchModeState
    ) { result, searchQuery, isInSearchMode ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (isInSearchMode && searchQuery.isNotBlank()) {
            result.map { grouped ->
                grouped.map { GroupedItemList(it.key, it.items.filterByQuery(searchQuery)) }
            }
        } else {
            result
        }
    }.flowOn(appDispatchers.default)

    private val resultsFlow = combine(
        filteredSearchEntriesFlow,
        textFilterListItemFlow,
        searchOptionsFlow.map { it.filterOption },
        isInSuggestionsModeState,
        isInSearchModeState
    ) { recentSearchResult, result, searchFilterType, isInSuggestionsMode, isInSearchMode ->
        if (isInSuggestionsMode && isInSearchMode) {
            recentSearchResult
        } else {
            result.map { grouped ->
                grouped
                    .map {
                        GroupedItemList(
                            it.key,
                            it.items.filterByType(searchFilterType.searchFilterType)
                        )
                    }
                    .filter { it.items.isNotEmpty() }
                    .toImmutableList()
            }
        }
    }.flowOn(appDispatchers.default)

    private val pinningUiStateFlow = combine(
        observePinnedItems().asLoadingResult(),
        searchOptionsFlow,
        isInSeeAllPinsModeState,
        debouncedSearchQueryState
    ) { pinnedItemsResult, searchOptions, isInSeeAllPinsMode, searchQuery ->
        val pinnedItems = pinnedItemsResult.getOrNull()?.let { list ->
            encryptionContextProvider.withEncryptionContext {
                list.map { it.toUiModel(this@withEncryptionContext) }
            }
        } ?: emptyList()
        val sortedPinnedItems = pinnedItems.sortItemLists(searchOptions.sortingOption)
            .toPersistentList()
        val textFilteredPinnedItems = pinnedItems
            .filterByQuery(searchQuery)
        val typeFilteredPinnedItems = textFilteredPinnedItems
            .filterByType(searchOptions.filterOption.searchFilterType)
        val groupedItems = typeFilteredPinnedItems
            .groupedItemLists(searchOptions.sortingOption, clock.now())
            .toPersistentList()
        PinningUiState(
            inPinningMode = isInSeeAllPinsMode,
            filteredItems = groupedItems,
            unFilteredItems = sortedPinnedItems,
            itemTypeCount = ItemTypeCount(
                loginCount = textFilteredPinnedItems.count { it.contents is ItemContents.Login },
                aliasCount = textFilteredPinnedItems.count { it.contents is ItemContents.Alias },
                noteCount = textFilteredPinnedItems.count { it.contents is ItemContents.Note },
                creditCardCount = textFilteredPinnedItems.count { it.contents is ItemContents.CreditCard },
                identityCount = textFilteredPinnedItems.count { it.contents is ItemContents.Identity }
            )
        )
    }

    private val refreshingLoadingFlow = combine(
        isRefreshing,
        actionStateFlow,
        ::ActionRefreshingWrapper
    ).distinctUntilChanged()

    private val itemTypeCountFlow = textFilterListItemFlow.map { result ->
        when (result) {
            is LoadingResult.Error -> ItemTypeCount.Initial
            LoadingResult.Loading -> ItemTypeCount.Initial
            is LoadingResult.Success -> {
                result.data.map { it.items }.flatten().let { list ->
                    ItemTypeCount(
                        loginCount = list.count { it.contents is ItemContents.Login },
                        aliasCount = list.count { it.contents is ItemContents.Alias },
                        noteCount = list.count { it.contents is ItemContents.Note },
                        creditCardCount = list.count { it.contents is ItemContents.CreditCard },
                        identityCount = list.count { it.contents is ItemContents.Identity }
                    )
                }
            }
        }
    }.distinctUntilChanged()

    private val searchUiStateFlow = combine(
        searchQueryState,
        isProcessingSearchState,
        isInSearchModeState,
        isInSuggestionsModeState,
        itemTypeCountFlow,
        ::SearchUiState
    )

    private val itemsFlow: Flow<LoadingResult<ImmutableList<GroupedItemList>>> = combine(
        shareListWrapperFlow,
        resultsFlow
    ) { shares, items ->
        items.map { listOfGroupedItems ->
            listOfGroupedItems.map { groupedItemList ->
                val mappedItems = groupedItemList.items.map { item ->
                    item.copy(canModify = checkCanModify(shares, item.shareId))
                }
                groupedItemList.copy(items = mappedItems.toImmutableList())
            }.toImmutableList()
        }
    }.flowOn(appDispatchers.default)

    private val appNeedsUpdateFlow: Flow<LoadingResult<Boolean>> = observeAppNeedsUpdate()
        .asLoadingResult()
        .distinctUntilChanged()

    private val homeListUiStateFlow = combineN(
        itemsFlow,
        refreshingLoadingFlow,
        shouldScrollToTopFlow,
        searchOptionsFlow,
        shareListWrapperFlow,
        preferencesRepository.getUseFaviconsPreference(),
        selectionState,
        appNeedsUpdateFlow
    ) { itemsResult,
        refreshingLoading,
        shouldScrollToTop,
        searchOptions,
        shareListWrapper,
        useFavicons,
        selection,
        appNeedsUpdate ->
        val isLoadingState = IsLoadingState.from(itemsResult is LoadingResult.Loading)

        val (items, isLoading) = when (itemsResult) {
            LoadingResult.Loading -> persistentListOf<GroupedItemList>() to IsLoadingState.Loading
            is LoadingResult.Success -> itemsResult.data to isLoadingState

            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Observe items error")
                PassLogger.e(TAG, itemsResult.exception)
                snackbarDispatcher(ObserveItemsError)
                persistentListOf<GroupedItemList>() to IsLoadingState.NotLoading
            }
        }

        HomeListUiState(
            isLoading = isLoading,
            isRefreshing = refreshingLoading.refreshing,
            shouldScrollToTop = shouldScrollToTop,
            actionState = refreshingLoading.actionState,
            items = items,
            selectedShare = shareListWrapper.selectedShare,
            shares = shareListWrapper.shares,
            homeVaultSelection = searchOptions.vaultSelectionOption,
            searchFilterType = searchOptions.filterOption.searchFilterType,
            sortingType = searchOptions.sortingOption.searchSortingType,
            canLoadExternalImages = useFavicons.value(),
            selectionState = selection.toState(
                isTrash = searchOptions.vaultSelectionOption == VaultSelectionOption.Trash
            ),
            showNeedsUpdate = appNeedsUpdate.getOrNull() ?: false
        )
    }

    private val bottomSheetItemActionFlow: MutableStateFlow<BottomSheetItemAction> =
        MutableStateFlow(BottomSheetItemAction.None)

    internal val homeUiState: StateFlow<HomeUiState> = combineN(
        homeListUiStateFlow,
        searchUiStateFlow,
        getUserPlan().asLoadingResult(),
        navEventState,
        pinningUiStateFlow,
        bottomSheetItemActionFlow,
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.SL_ALIASES_SYNC),
        preferencesRepository.observeAliasTrashDialogStatusPreference()
    ) { homeListUiState,
        searchUiState,
        userPlan,
        navEvent,
        pinningUiState,
        bottomSheetItemAction,
        isSLAliasSyncEnabled,
        aliasTrashDialogStatusPreference ->
        HomeUiState(
            homeListUiState = homeListUiState,
            searchUiState = searchUiState,
            pinningUiState = pinningUiState,
            accountType = AccountType.fromPlan(userPlan),
            navEvent = navEvent,
            action = bottomSheetItemAction,
            isFreePlan = userPlan.map { plan -> plan.isFreePlan }.getOrNull() ?: true,
            isSLAliasSyncEnabled = isSLAliasSyncEnabled,
            aliasTrashDialogStatusPreference = aliasTrashDialogStatusPreference
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = HomeUiState.Loading
        )


    init {
        viewModelScope.launch {
            // Setup initial share id if we can get one from the route
            val initialShareId: String? = savedState.get()[CommonOptionalNavArgId.ShareId.key]
            if (initialShareId != null) {
                val vaultSelection = VaultSelectionOption.Vault(ShareId(initialShareId))
                observeCurrentUser().firstOrNull()?.let {
                    homeSearchOptionsRepository.setVaultSelectionOption(it.userId, vaultSelection)
                }
            }
        }


        // Observe bulkMoveToVault event
        viewModelScope.launch {
            bulkMoveToVaultRepository.observeEvent().collect { event ->
                if (event is BulkMoveToVaultEvent.Completed) {
                    clearSelection()
                    bulkMoveToVaultRepository.emitEvent(BulkMoveToVaultEvent.Idle)
                }
            }
        }

    }

    fun onSearchQueryChange(query: String) {
        if (query.contains("\n")) return

        searchQueryState.update { query }
        isInSuggestionsModeState.update { false }
        isProcessingSearchState.update { IsProcessingSearchState.Loading }
    }

    fun onStopSearching() {
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
        isInSuggestionsModeState.update { false }
    }

    fun onStopSeeAllPinned() {
        isInSeeAllPinsModeState.update { false }
    }

    fun onEnterSearch() {
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
        if (searchEntryState.value.isNotEmpty()) {
            isInSuggestionsModeState.update { true }
        }
        if (!hasEnteredSearch) {
            telemetryManager.sendEvent(SearchTriggered)
        }
        hasEnteredSearch = true
    }

    fun onRefresh() = viewModelScope.launch(coroutineExceptionHandler) {
        isRefreshing.update { IsRefreshingState.Refreshing }

        runCatching {
            val userId = observeCurrentUser().firstOrNull()?.userId
                ?: throw IllegalStateException("User not found")
            performSync(userId)
        }.onFailure {
            PassLogger.e(TAG, it, "Apply pending events failed")
            snackbarDispatcher(RefreshError)
        }

        isRefreshing.update { IsRefreshingState.NotRefreshing }
    }

    internal fun sendItemsToTrash(items: List<ItemUiModel>) {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (items.isEmpty()) return@launch

            actionStateFlow.update { ActionState.Loading() }
            bottomSheetItemActionFlow.update { BottomSheetItemAction.Trash }

            val mappedItems = items.toShareIdItemId().toPersistentSet()
            val itemTypes = homeUiState.value.homeListUiState.items
                .flatMap { it.items }
                .filter { (itemId: ItemId, shareId: ShareId) -> mappedItems.contains(shareId to itemId) }

            val groupedItems = groupItems(mappedItems)
            runCatching { trashItems(items = groupedItems) }
                .onSuccess {
                    clearSelection()
                    if (itemTypes.size == 1) {
                        when (itemTypes.first().contents) {
                            is ItemContents.Alias -> snackbarDispatcher(AliasMovedToTrash)
                            is ItemContents.Login -> snackbarDispatcher(LoginMovedToTrash)
                            is ItemContents.Note -> snackbarDispatcher(NoteMovedToTrash)
                            is ItemContents.CreditCard -> snackbarDispatcher(CreditCardMovedToTrash)
                            is ItemContents.Identity -> snackbarDispatcher(IdentityMovedToTrash)
                            is ItemContents.Unknown -> {}
                        }
                    } else {
                        snackbarDispatcher(ItemsMovedToTrashSuccess)
                    }
                }
                .onFailure {
                    PassLogger.w(TAG, "Trash items failed")
                    PassLogger.w(TAG, it)
                    if (itemTypes.size == 1) {
                        snackbarDispatcher(MoveToTrashError)
                    } else {
                        snackbarDispatcher(ItemsMovedToTrashError)
                    }
                }

            actionStateFlow.update { ActionState.Done }
            bottomSheetItemActionFlow.update { BottomSheetItemAction.None }
        }
    }

    fun copyToClipboard(text: String, homeClipboardType: HomeClipboardType) {
        val sanitizedText = text.take(MAX_CLIPBOARD_LENGTH)
        viewModelScope.launch {
            val message = when (homeClipboardType) {
                HomeClipboardType.Alias -> {
                    clipboardManager.copyToClipboard(text = sanitizedText)
                    HomeSnackbarMessage.AliasCopied
                }

                HomeClipboardType.Note -> {
                    clipboardManager.copyToClipboard(text = sanitizedText)

                    HomeSnackbarMessage.NoteCopied
                }

                HomeClipboardType.Password -> {
                    clipboardManager.copyToClipboard(
                        text = encryptionContextProvider.withEncryptionContext { decrypt(text) },
                        isSecure = true
                    )
                    HomeSnackbarMessage.PasswordCopied
                }

                HomeClipboardType.Email -> {
                    clipboardManager.copyToClipboard(text = sanitizedText)
                    HomeSnackbarMessage.EmailCopied
                }

                HomeClipboardType.Username -> {
                    clipboardManager.copyToClipboard(text = sanitizedText)
                    HomeSnackbarMessage.UsernameCopied
                }

                HomeClipboardType.CreditCardNumber -> {
                    clipboardManager.copyToClipboard(
                        text = sanitizedText,
                        isSecure = true
                    )
                    HomeSnackbarMessage.CreditCardNumberCopied
                }

                HomeClipboardType.CreditCardCvv -> {
                    clipboardManager.copyToClipboard(
                        text = encryptionContextProvider.withEncryptionContext { decrypt(text) },
                        isSecure = true
                    )
                    HomeSnackbarMessage.CreditCardCvvCopied
                }

                HomeClipboardType.FullName -> {
                    clipboardManager.copyToClipboard(text = sanitizedText)
                    HomeSnackbarMessage.FullNameCopied
                }
            }

            val snackbarMessage = if (text.length > MAX_CLIPBOARD_LENGTH) {
                HomeSnackbarMessage.ItemTooLongCopied
            } else {
                message
            }

            snackbarDispatcher(snackbarMessage)
        }
    }

    internal fun pinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        bottomSheetItemActionFlow.update { BottomSheetItemAction.Pin }

        runCatching { pinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(HomeSnackbarMessage.ItemPinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred pinning home item")
                snackbarDispatcher(HomeSnackbarMessage.ItemPinnedError)
            }

        bottomSheetItemActionFlow.update { BottomSheetItemAction.None }
    }

    internal fun unpinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        bottomSheetItemActionFlow.update { BottomSheetItemAction.Unpin }

        runCatching { unpinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(HomeSnackbarMessage.ItemUnpinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred unpinning home item")
                snackbarDispatcher(HomeSnackbarMessage.ItemUnpinnedError)
            }

        bottomSheetItemActionFlow.update { BottomSheetItemAction.None }
    }

    internal fun viewItemHistory(shareId: ShareId, itemId: ItemId) {
        homeUiState.value
            .let { state ->
                if (state.isFreePlan) {
                    HomeNavEvent.UpgradeDialog
                } else {
                    HomeNavEvent.ItemHistory(shareId, itemId)
                }
            }
            .also { homeNavEvent ->
                navEventState.update { homeNavEvent }
            }
    }

    fun setItemTypeSelection(searchFilterType: SearchFilterType) {
        homeSearchOptionsRepository.setFilterOption(FilterOption(searchFilterType))
        isInSuggestionsModeState.update { false }
    }

    fun setVaultSelection(vaultSelection: VaultSelectionOption) {
        viewModelScope.launch {
            observeCurrentUser().firstOrNull()?.let {
                homeSearchOptionsRepository.setVaultSelectionOption(it.userId, vaultSelection)
            }
        }
        homeSearchOptionsRepository.setFilterOption(FilterOption(SearchFilterType.All))
    }

    fun restoreActionState() {
        actionStateFlow.update { ActionState.Unknown }
    }

    fun restoreItems(items: List<ItemUiModel>) = viewModelScope.launch(coroutineExceptionHandler) {
        if (items.isEmpty()) return@launch
        actionStateFlow.update { ActionState.Loading() }

        val mappedItems = groupItems(items.toShareIdItemId().toPersistentSet())
        runCatching { restoreItems(items = mappedItems) }
            .onSuccess {
                clearSelection()
                snackbarDispatcher(RestoreItemsSuccess)
            }
            .onFailure {
                PassLogger.w(TAG, "Untrash items failed")
                PassLogger.w(TAG, it)
                snackbarDispatcher(RestoreItemsError)
            }
        actionStateFlow.update { ActionState.Done }
    }

    fun deleteItems(items: List<ItemUiModel>) = viewModelScope.launch(coroutineExceptionHandler) {
        if (items.isEmpty()) return@launch
        actionStateFlow.update { ActionState.Loading() }

        val mappedItems = items.toShareIdItemId()
        val itemTypes = homeUiState.value.homeListUiState.items
            .flatMap { it.items }
            .filter { (itemId: ItemId, shareId: ShareId) ->
                mappedItems.contains(shareId to itemId)
            }
            .map { EventItemType.from(it.contents) }

        runCatching {
            deleteItem(items = mappedItems.groupBy({ it.first }, { it.second }))
        }.onSuccess {
            PassLogger.i(TAG, "Items deleted successfully")
            clearSelection()
            itemTypes.forEach { telemetryManager.sendEvent(ItemDelete(it)) }
            if (items.size > 1) {
                snackbarDispatcher(DeleteItemsSuccess)
            } else {
                snackbarDispatcher(DeleteItemSuccess)
            }
        }.onFailure {
            PassLogger.w(TAG, "Error deleting items")
            PassLogger.w(TAG, it)
            if (items.size > 1) {
                snackbarDispatcher(DeleteItemsError)
            } else {
                snackbarDispatcher(DeleteItemError)
            }
        }
        actionStateFlow.update { ActionState.Done }
    }

    fun clearTrash() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading() }

        val deletedItems = homeUiState.value.homeListUiState.items
        runCatching {
            clearTrash.invoke()
        }.onSuccess {
            PassLogger.i(TAG, "Trash cleared successfully")
            emitDeletedItems(deletedItems)
        }.onFailure {
            PassLogger.w(TAG, "Error clearing trash")
            PassLogger.w(TAG, it)
            snackbarDispatcher(ClearTrashError)
        }
        actionStateFlow.update { ActionState.Done }
    }

    fun restoreAllItems() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading() }
        runCatching {
            restoreAllItems.invoke()
        }.onSuccess {
            PassLogger.i(TAG, "Items restored successfully")
        }.onFailure {
            PassLogger.w(TAG, "Error restoring items")
            PassLogger.w(TAG, it)
            snackbarDispatcher(RestoreItemsError)
        }
        actionStateFlow.update { ActionState.Done }
    }

    fun onItemClicked(shareId: ShareId, itemId: ItemId) {
        if (homeUiState.value.searchUiState.inSearchMode) {
            viewModelScope.launch {
                addSearchEntry(shareId, itemId)
            }
            telemetryManager.sendEvent(SearchItemClicked)
        }
    }

    fun onClearAllRecentSearch() {
        viewModelScope.launch {
            deleteAllSearchEntry()
            isInSuggestionsModeState.update { false }
        }
    }

    fun onClearRecentSearch(shareId: ShareId, itemId: ItemId) {
        viewModelScope.launch {
            deleteSearchEntry(shareId, itemId)
        }
    }

    fun onScrollToTop() {
        shouldScrollToTopFlow.update { false }
    }

    fun scrollToTop() {
        shouldScrollToTopFlow.update { true }
    }

    fun onBulkEnabled() {
        selectionState.update { state -> state.copy(isInSelectMode = true) }
    }

    fun onItemSelected(item: ItemUiModel) {
        selectionState.update { state ->
            val alreadyInList = state.selectedItems.any {
                it.shareId == item.shareId && it.id == item.id
            }
            if (alreadyInList) {
                val newSelectedItems = state.selectedItems.filterNot {
                    it.shareId == item.shareId && it.id == item.id
                }
                state.copy(
                    selectedItems = newSelectedItems,
                    isInSelectMode = newSelectedItems.isNotEmpty()
                )
            } else {
                state.copy(
                    selectedItems = state.selectedItems + item,
                    isInSelectMode = true
                )
            }
        }
    }

    internal fun onReadOnlyItemSelected() {
        toastManager.showToast(R.string.home_toast_items_selected_read_only)
    }

    fun clearSelection() {
        selectionState.update { SelectionState.Initial }
    }

    fun moveItemsToVault(items: List<ItemUiModel>) = viewModelScope.launch {
        val selectedItemsAsPairs = items.map { it.shareId to it.id }.toPersistentSet()
        val groupedItems = groupItems(selectedItemsAsPairs)
        bulkMoveToVaultRepository.save(groupedItems)
        navEventState.update { HomeNavEvent.ShowBulkMoveToVault }
    }

    fun pinSelectedItems(items: List<ItemUiModel>) = viewModelScope.launch {
        val nonPinnedItems = items.filter { !it.isPinned }.toShareIdItemId()
        if (nonPinnedItems.isEmpty()) {
            PassLogger.w(TAG, "No items to be pinned")
            return@launch
        }

        selectionState.update { it.copy(pinningLoadingState = IsLoadingState.Loading) }
        runCatching {
            pinItems(nonPinnedItems)
        }.onSuccess {
            when (it) {
                is PinItemsResult.AllPinned -> {
                    PassLogger.i(TAG, "All items pinned successfully")
                    snackbarDispatcher(ItemsPinnedSuccess)
                    clearSelection()
                }

                is PinItemsResult.NonePinned -> {
                    PassLogger.w(TAG, "Could not pin any item")
                    snackbarDispatcher(ItemsPinnedError)
                }

                is PinItemsResult.SomePinned -> {
                    PassLogger.w(TAG, "Could not pin any item")
                    snackbarDispatcher(ItemsPinnedPartialSuccess)
                    clearSelection()
                }
            }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error pinning items")
            PassLogger.w(TAG, error)
            selectionState.update { it.copy(pinningLoadingState = IsLoadingState.NotLoading) }
            snackbarDispatcher(ItemsPinnedError)
        }
    }

    fun unpinSelectedItems(items: List<ItemUiModel>) = viewModelScope.launch {
        val pinnedItems = items.filter { it.isPinned }.toShareIdItemId()
        if (pinnedItems.isEmpty()) {
            PassLogger.w(TAG, "No items to be unpinned")
            return@launch
        }

        selectionState.update { it.copy(pinningLoadingState = IsLoadingState.Loading) }
        runCatching {
            unpinItems(pinnedItems)
        }.onSuccess {
            when (it) {
                is PinItemsResult.AllPinned -> {
                    snackbarDispatcher(ItemsUnpinnedSuccess)
                    clearSelection()
                }

                is PinItemsResult.NonePinned -> {
                    snackbarDispatcher(ItemsUnpinnedError)
                }

                is PinItemsResult.SomePinned -> {
                    snackbarDispatcher(ItemsUnpinnedPartialSuccess)
                    clearSelection()
                }
            }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error unpinning items")
            PassLogger.w(TAG, error)
            selectionState.update { it.copy(pinningLoadingState = IsLoadingState.NotLoading) }
            snackbarDispatcher(ItemsUnpinnedError)
        }
    }

    internal fun onNavEventConsumed(event: HomeNavEvent) {
        navEventState.compareAndSet(event, HomeNavEvent.Unknown)
    }

    fun onSeeAllPinned() {
        isInSeeAllPinsModeState.update { true }
    }

    fun openUpdateApp(contextHolder: ClassHolder<Context>) {
        contextHolder.get().map {
            BrowserUtils.openWebsite(it, AppUrls.PASS_STORE)
        }
    }

    private fun List<ItemUiModel>.filterByType(searchFilterType: SearchFilterType) = filter { item ->
        when (searchFilterType) {
            SearchFilterType.All -> true
            SearchFilterType.Alias -> item.contents is ItemContents.Alias
            SearchFilterType.Login -> item.contents is ItemContents.Login
            SearchFilterType.Note -> item.contents is ItemContents.Note
            SearchFilterType.CreditCard -> item.contents is ItemContents.CreditCard
            SearchFilterType.Identity -> item.contents is ItemContents.Identity
            SearchFilterType.LoginMFA ->
                item.contents is ItemContents.Login && (item.contents as ItemContents.Login).hasPrimaryTotp
        }
    }

    private fun emitDeletedItems(items: List<GroupedItemList>) {
        items.forEach { list ->
            list.items.forEach { item ->
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(item.contents)))
            }
        }
    }

    private fun checkCanModify(listWrapper: ShareListWrapper, shareId: ShareId): Boolean =
        listWrapper.shares[shareId]?.role?.toPermissions()?.canUpdate() ?: false

    private fun groupItems(items: ImmutableSet<Pair<ShareId, ItemId>>): Map<ShareId, List<ItemId>> =
        items.groupBy({ it.first }, { it.second })

    private fun List<ItemUiModel>.toShareIdItemId(): List<Pair<ShareId, ItemId>> = map { it.shareId to it.id }

    private fun List<ItemUiModel>.sortItemLists(sortingOption: SortingOption) = when (sortingOption.searchSortingType) {
        SearchSortingType.MostRecent -> sortMostRecent()
        SearchSortingType.TitleAsc -> sortByTitleAsc()
        SearchSortingType.TitleDesc -> sortByTitleDesc()
        SearchSortingType.CreationAsc -> sortByCreationAsc()
        SearchSortingType.CreationDesc -> sortByCreationDesc()
    }

    private fun List<ItemUiModel>.groupedItemLists(sortingOption: SortingOption, instant: Instant) =
        when (sortingOption.searchSortingType) {
            SearchSortingType.MostRecent -> groupAndSortByMostRecent(instant)
            SearchSortingType.TitleAsc -> groupAndSortByTitleAsc()
            SearchSortingType.TitleDesc -> groupAndSortByTitleDesc()
            SearchSortingType.CreationAsc -> groupAndSortByCreationAsc()
            SearchSortingType.CreationDesc -> groupAndSortByCreationDesc()
        }

    private fun autoSelectAllVaultsIfCannotFindVault(
        selectedShare: Option<ShareUiModel>,
        vaultList: List<Vault>,
        userId: UserId?
    ) {
        userId ?: return
        val doAllVaultsBelongToUser = vaultList.all { it.userId == userId }
        if (selectedShare is None && vaultList.isNotEmpty() && doAllVaultsBelongToUser) {
            homeSearchOptionsRepository.setVaultSelectionOption(
                userId,
                VaultSelectionOption.AllVaults
            )
        }
    }

    private fun autoSelectVaultIfSingle(
        vaults: List<Vault>,
        vaultSelection: VaultSelectionOption,
        userId: UserId?
    ) {
        if (vaults.size == 1 && vaultSelection is VaultSelectionOption.AllVaults && userId != null) {
            val selection = VaultSelectionOption.Vault(vaults.first().shareId)
            homeSearchOptionsRepository.setVaultSelectionOption(userId, selection)
        }
    }

    fun disableAlias(item: ItemUiModel) {
        viewModelScope.launch {
            actionStateFlow.update { ActionState.Loading(LoadingDialog.DisableAlias) }
            runCatching {
                changeAliasStatus(listOf(item).toShareIdItemId(), false)
            }.onSuccess {
                when (it) {
                    is AliasItemsChangeStatusResult.AllChanged -> {
                        snackbarDispatcher(AliasItemsEnabledSuccess)
                        clearSelection()
                    }

                    is AliasItemsChangeStatusResult.NoneChanged -> {
                        snackbarDispatcher(AliasItemsDisabledError)
                    }

                    is AliasItemsChangeStatusResult.SomeChanged -> {
                        snackbarDispatcher(AliasItemsEnabledSuccess)
                        clearSelection()
                    }
                }
            }.onFailure { error ->
                PassLogger.w(TAG, "Error disabling alias items")
                PassLogger.w(TAG, error)
                snackbarDispatcher(AliasItemsDisabledError)
            }
            actionStateFlow.update { ActionState.Done }
        }
    }

    fun disableSelectedAliasItems(items: List<ItemUiModel>) = viewModelScope.launch {
        val aliasItems = items.filter { it.contents is ItemContents.Alias }.toShareIdItemId()
        if (aliasItems.isEmpty()) {
            PassLogger.w(TAG, "No items to be disabled")
            return@launch
        }

        selectionState.update { it.copy(aliasLoadingState = IsLoadingState.Loading) }
        runCatching {
            changeAliasStatus(aliasItems, false)
        }.onSuccess {
            when (it) {
                is AliasItemsChangeStatusResult.AllChanged -> {
                    snackbarDispatcher(AliasItemsEnabledSuccess)
                    clearSelection()
                }

                is AliasItemsChangeStatusResult.NoneChanged -> {
                    snackbarDispatcher(AliasItemsDisabledError)
                }

                is AliasItemsChangeStatusResult.SomeChanged -> {
                    snackbarDispatcher(AliasItemsEnabledSuccess)
                    clearSelection()
                }
            }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error disabling alias items")
            PassLogger.w(TAG, error)
            selectionState.update { it.copy(pinningLoadingState = IsLoadingState.NotLoading) }
            snackbarDispatcher(AliasItemsDisabledError)
        }
    }

    fun enableSelectedAliasItems(items: ImmutableList<ItemUiModel>) = viewModelScope.launch {
        val aliasItems = items.filter { it.contents is ItemContents.Alias }.toShareIdItemId()
        if (aliasItems.isEmpty()) {
            PassLogger.w(TAG, "No items to be enabled")
            return@launch
        }

        selectionState.update { it.copy(aliasLoadingState = IsLoadingState.Loading) }
        runCatching {
            changeAliasStatus(aliasItems, true)
        }.onSuccess {
            when (it) {
                is AliasItemsChangeStatusResult.AllChanged -> {
                    snackbarDispatcher(AliasItemsEnabledSuccess)
                    clearSelection()
                }

                is AliasItemsChangeStatusResult.NoneChanged -> {
                    snackbarDispatcher(AliasItemsEnabledError)
                }

                is AliasItemsChangeStatusResult.SomeChanged -> {
                    snackbarDispatcher(AliasItemsEnabledPartialSuccess)
                    clearSelection()
                }
            }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error enabling alias items")
            PassLogger.w(TAG, error)
            selectionState.update { it.copy(aliasLoadingState = IsLoadingState.NotLoading) }
            snackbarDispatcher(AliasItemsEnabledError)
        }
    }

    private data class SelectionState(
        val selectedItems: List<ItemUiModel>,
        val isInSelectMode: Boolean,
        val pinningLoadingState: IsLoadingState,
        val aliasLoadingState: IsLoadingState
    ) {
        fun toState(isTrash: Boolean) = HomeSelectionState(
            selectedItems = selectedItems.toPersistentList(),
            isInSelectMode = isInSelectMode,
            topBarState = SelectionTopBarState(
                isTrash = isTrash,
                selectedItemCount = selectedItems.size,
                pinningState = PinningState(
                    areAllSelectedPinned = selectedItems.all { it.isPinned },
                    pinningLoadingState = pinningLoadingState
                ),
                aliasState = AliasState(
                    areAllSelectedAliases = selectedItems.all { it.contents is ItemContents.Alias },
                    areAllSelectedDisabled = selectedItems.all {
                        (it.contents as? ItemContents.Alias)?.isEnabled == false
                    },
                    aliasLoadingState = aliasLoadingState
                ),
                // Actions are only enabled if there are selected items and the pinning operation
                // is not in progress
                actionsEnabled = selectedItems.isNotEmpty() &&
                    !pinningLoadingState.value() &&
                    !aliasLoadingState.value()
            )
        )

        companion object {
            val Initial = SelectionState(
                selectedItems = emptyList(),
                isInSelectMode = false,
                pinningLoadingState = IsLoadingState.NotLoading,
                aliasLoadingState = IsLoadingState.NotLoading
            )
        }
    }

    companion object {
        private const val ITEM_DELETED_THRESHOLD = 5
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
        private const val MAX_CLIPBOARD_LENGTH = 2500
    }
}

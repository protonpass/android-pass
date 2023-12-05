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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys.NoGrouping
import proton.android.pass.commonui.api.ItemSorter.sortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.sortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.sortByMostRecent
import proton.android.pass.commonui.api.ItemSorter.sortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.sortByTitleDesc
import proton.android.pass.commonui.api.ItemUiFilter.filterByQuery
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.BulkMoveToVaultEvent
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RestoreAllItems
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItems
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
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ClearTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.CreditCardMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemsSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsMovedToTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ItemsMovedToTrashSuccess
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.LoginMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.MoveToTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ObserveItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RefreshError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RestoreItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RestoreItemsSuccess
import proton.android.pass.featuresearchoptions.api.FilterOption
import proton.android.pass.featuresearchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SearchFilterType
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
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
    observeVaults: ObserveVaults,
    clock: Clock,
    observeItems: ObserveItems,
    preferencesRepository: UserPreferencesRepository,
    getUserPlan: GetUserPlan,
    appDispatchers: AppDispatchers,
    savedState: SavedStateHandleProvider
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
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
        searchOptionsFlow.map { it.vaultSelectionOption },
        observeVaults().asLoadingResult()
    ) { vaultSelection, vaultsResult ->
        val vaults: List<Vault> = vaultsResult.getOrNull() ?: emptyList()
        if (vaults.size == 1 && vaultSelection is VaultSelectionOption.AllVaults) {
            homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.Vault(vaults.first().shareId))
        }
        val shares: PersistentMap<ShareId, ShareUiModel> = vaults.associate {
            it.shareId to ShareUiModel.fromVault(it)
        }.toPersistentMap()
        val selectedShare: Option<ShareUiModel> = when (vaultSelection) {
            VaultSelectionOption.AllVaults -> None
            VaultSelectionOption.Trash -> None
            is VaultSelectionOption.Vault -> {
                val match: Option<Vault> = vaults
                    .firstOrNull { it.shareId == vaultSelection.shareId }
                    .toOption()
                if (match is None && vaults.isNotEmpty()) {
                    homeSearchOptionsRepository.setVaultSelectionOption(VaultSelectionOption.AllVaults)
                }
                match.map { ShareUiModel.fromVault(it) }
            }
        }
        ShareListWrapper(
            shares = shares,
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

            observeItems(
                selection = shareSelection,
                itemState = itemState,

                // We observe them all, because otherwise in the All part of the search we would not
                // know how many ItemTypes are there for the other ItemTypes.
                // We filter out the results using the filterByType function
                filter = ItemTypeFilter.All
            ).asResultWithoutLoading()
                .map { itemResult ->
                    itemResult.map { list ->
                        encryptionContextProvider.withEncryptionContext {
                            list.map { it.toUiModel(this@withEncryptionContext) }
                        }
                    }
                }
                .distinctUntilChanged()
        }

    private val filteredSearchEntriesFlow = combine(
        itemUiModelFlow,
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
        when (sortingOption.searchSortingType) {
            SearchSortingType.TitleAsc -> result.map { list -> list.sortByTitleAsc() }
            SearchSortingType.TitleDesc -> result.map { list -> list.sortByTitleDesc() }
            SearchSortingType.CreationAsc -> result.map { list -> list.sortByCreationAsc() }
            SearchSortingType.CreationDesc -> result.map { list -> list.sortByCreationDesc() }
            SearchSortingType.MostRecent -> result.map { list -> list.sortByMostRecent(clock.now()) }
        }
    }.distinctUntilChanged()

    private val textFilterListItemFlow = combine(
        sortedListItemFlow,
        debouncedSearchQueryState,
        isInSearchModeState
    ) { result, searchQuery, isInSearchMode ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (isInSearchMode && searchQuery.isNotBlank()) {
            result.map { grouped ->
                grouped.map { GroupedItemList(it.key, filterByQuery(it.items, searchQuery)) }
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
                            filterByType(it.items, searchFilterType.searchFilterType)
                        )
                    }
                    .filter { it.items.isNotEmpty() }
                    .toImmutableList()
            }
        }
    }.flowOn(appDispatchers.default)

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
                        creditCardCount = list.count { it.contents is ItemContents.CreditCard }
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

    val homeUiState: StateFlow<HomeUiState> = combineN(
        shareListWrapperFlow,
        searchOptionsFlow,
        itemsFlow,
        searchUiStateFlow,
        refreshingLoadingFlow,
        shouldScrollToTopFlow,
        preferencesRepository.getUseFaviconsPreference(),
        getUserPlan().asLoadingResult(),
        selectionState,
        navEventState
    ) { shareListWrapper, searchOptions, itemsResult, searchUiState, refreshingLoading,
        shouldScrollToTop, useFavicons, userPlan, selection, navEvent ->
        val isLoadingState = IsLoadingState.from(itemsResult is LoadingResult.Loading)

        val (items, isLoading) = when (itemsResult) {
            LoadingResult.Loading -> persistentListOf<GroupedItemList>() to IsLoadingState.Loading
            is LoadingResult.Success -> itemsResult.data to isLoadingState

            is LoadingResult.Error -> {
                PassLogger.e(TAG, itemsResult.exception, "Observe items error")
                snackbarDispatcher(ObserveItemsError)
                persistentListOf<GroupedItemList>() to IsLoadingState.NotLoading
            }
        }
        HomeUiState(
            homeListUiState = HomeListUiState(
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
                selectionState = selection.toState()
            ),
            searchUiState = searchUiState,
            accountType = AccountType.fromPlan(userPlan),
            navEvent = navEvent
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = HomeUiState.Loading
        )


    init {
        // Setup initial share id if we can get one from the route
        val initialShareId: String? = savedState.get()[CommonOptionalNavArgId.ShareId.key]
        if (initialShareId != null) {
            val vaultSelection = VaultSelectionOption.Vault(ShareId(initialShareId))
            homeSearchOptionsRepository.setVaultSelectionOption(vaultSelection)
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
            performSync()
        }.onFailure {
            PassLogger.e(TAG, it, "Apply pending events failed")
            snackbarDispatcher(RefreshError)
        }

        isRefreshing.update { IsRefreshingState.NotRefreshing }
    }

    fun sendItemsToTrash(items: ImmutableSet<Pair<ShareId, ItemId>>) =
        viewModelScope.launch(coroutineExceptionHandler) {
            if (items.isEmpty()) return@launch
            actionStateFlow.update { ActionState.Loading }
            val itemTypes = homeUiState.value.homeListUiState.items
                .flatMap { it.items }
                .filter { (itemId: ItemId, shareId: ShareId) -> items.contains(shareId to itemId) }

            val groupedItems = groupItems(items)
            runCatching { trashItems(items = groupedItems) }
                .onSuccess {
                    clearSelection()
                    if (itemTypes.size == 1) {
                        when (itemTypes.first().contents) {
                            is ItemContents.Alias -> snackbarDispatcher(AliasMovedToTrash)
                            is ItemContents.Login -> snackbarDispatcher(LoginMovedToTrash)
                            is ItemContents.Note -> snackbarDispatcher(NoteMovedToTrash)
                            is ItemContents.CreditCard -> snackbarDispatcher(CreditCardMovedToTrash)
                            is ItemContents.Unknown -> {}
                        }
                    } else {
                        snackbarDispatcher(ItemsMovedToTrashSuccess)
                    }
                }
                .onFailure {
                    PassLogger.e(TAG, it, "Trash items failed")
                    if (itemTypes.size == 1) {
                        snackbarDispatcher(MoveToTrashError)
                    } else {
                        snackbarDispatcher(ItemsMovedToTrashError)
                    }
                }
            actionStateFlow.update { ActionState.Done }
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
            }

            val snackbarMessage = if (text.length > MAX_CLIPBOARD_LENGTH) {
                HomeSnackbarMessage.ItemTooLongCopied
            } else {
                message
            }

            snackbarDispatcher(snackbarMessage)
        }
    }

    fun setItemTypeSelection(searchFilterType: SearchFilterType) {
        homeSearchOptionsRepository.setFilterOption(FilterOption(searchFilterType))
        isInSuggestionsModeState.update { false }
    }

    fun setVaultSelection(vaultSelection: VaultSelectionOption) {
        PassLogger.d(TAG, "Setting vault selection: $vaultSelection")
        homeSearchOptionsRepository.setVaultSelectionOption(vaultSelection)
        homeSearchOptionsRepository.setFilterOption(FilterOption(SearchFilterType.All))
    }

    fun restoreActionState() {
        actionStateFlow.update { ActionState.Unknown }
    }

    fun restoreItems(items: ImmutableSet<Pair<ShareId, ItemId>>) =
        viewModelScope.launch(coroutineExceptionHandler) {
            if (items.isEmpty()) return@launch
            actionStateFlow.update { ActionState.Loading }

            runCatching { restoreItems(items = items.groupBy({ it.first }, { it.second })) }
                .onSuccess {
                    clearSelection()
                    snackbarDispatcher(RestoreItemsSuccess)
                }
                .onFailure {
                    PassLogger.e(TAG, it, "Untrash items failed")
                    snackbarDispatcher(RestoreItemsError)
                }
            actionStateFlow.update { ActionState.Done }
        }

    fun deleteItems(items: ImmutableSet<Pair<ShareId, ItemId>>) =
        viewModelScope.launch(coroutineExceptionHandler) {
            if (items.isEmpty()) return@launch
            actionStateFlow.update { ActionState.Loading }
            val itemTypes = homeUiState.value.homeListUiState.items
                .flatMap { it.items }
                .filter { (itemId: ItemId, shareId: ShareId) -> items.contains(shareId to itemId) }
                .map { EventItemType.from(it.contents) }

            runCatching {
                deleteItem(items = items.groupBy({ it.first }, { it.second }))
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
                PassLogger.e(TAG, it, "Error deleting items")
                if (items.size > 1) {
                    snackbarDispatcher(DeleteItemsError)
                } else {
                    snackbarDispatcher(DeleteItemError)
                }
            }
            actionStateFlow.update { ActionState.Done }
        }

    fun clearTrash() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }

        val deletedItems = homeUiState.value.homeListUiState.items
        runCatching {
            clearTrash.invoke()
        }.onSuccess {
            PassLogger.i(TAG, "Trash cleared successfully")
            emitDeletedItems(deletedItems)
        }.onFailure {
            PassLogger.e(TAG, it, "Error clearing trash")
            snackbarDispatcher(ClearTrashError)
        }
        actionStateFlow.update { ActionState.Done }
    }

    fun restoreAllItems() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }
        runCatching {
            restoreAllItems.invoke()
        }.onSuccess {
            PassLogger.i(TAG, "Items restored successfully")
        }.onFailure {
            PassLogger.e(TAG, it, "Error restoring items")
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

    fun onBulkEnabled() = viewModelScope.launch {
        selectionState.update { state ->
            state.copy(isInSelectMode = true)
        }
    }

    fun onClearBulk() = viewModelScope.launch {
        selectionState.update { SelectionState.Initial }
    }

    fun onItemLongPressed(item: ItemUiModel) = viewModelScope.launch {
        selectionState.update { state ->
            if (state.isInSelectMode) {
                state.copy(selectedItems = state.selectedItems + item)
            } else {
                state.copy(isInSelectMode = true, selectedItems = listOf(item))
            }
        }
    }

    fun onItemSelected(item: ItemUiModel) = viewModelScope.launch {
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

    fun clearSelection() = viewModelScope.launch {
        selectionState.update { SelectionState.Initial }
    }

    fun moveItemsToVault(items: ImmutableSet<Pair<ShareId, ItemId>>) = viewModelScope.launch {
        val groupedItems = groupItems(items)
        bulkMoveToVaultRepository.save(groupedItems)
        navEventState.update { HomeNavEvent.ShowBulkMoveToVault }
    }

    fun clearNavEvent() = viewModelScope.launch {
        navEventState.update { HomeNavEvent.Unknown }
    }

    private fun filterByType(
        items: List<ItemUiModel>,
        searchFilterType: SearchFilterType
    ) = items.filter { item ->
        when (searchFilterType) {
            SearchFilterType.All -> true
            SearchFilterType.Alias -> item.contents is ItemContents.Alias
            SearchFilterType.Login -> item.contents is ItemContents.Login
            SearchFilterType.Note -> item.contents is ItemContents.Note
            SearchFilterType.CreditCard -> item.contents is ItemContents.CreditCard
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

    private data class SelectionState(
        val selectedItems: List<ItemUiModel>,
        val isInSelectMode: Boolean
    ) {
        fun toState(): HomeSelectionState = HomeSelectionState(
            selectedItems = selectedItems.map { it.shareId to it.id }.toPersistentSet(),
            isInSelectMode = isInSelectMode
        )

        companion object {
            val Initial = SelectionState(emptyList(), false)
        }
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
        private const val MAX_CLIPBOARD_LENGTH = 2500
    }
}

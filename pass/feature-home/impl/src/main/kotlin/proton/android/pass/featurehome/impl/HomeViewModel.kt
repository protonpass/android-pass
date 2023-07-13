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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
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
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry.SearchEntrySelection
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ClearTrashError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.DeleteItemError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.LoginMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ObserveItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RefreshError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RestoreItemsError
import proton.android.pass.featuresearchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.Vault
import javax.inject.Inject

@Suppress("LongParameterList", "LargeClass")
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trashItem: TrashItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val applyPendingEvents: ApplyPendingEvents,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val restoreItem: RestoreItem,
    private val restoreItems: RestoreItems,
    private val deleteItem: DeleteItem,
    private val clearTrash: ClearTrash,
    private val addSearchEntry: AddSearchEntry,
    private val deleteSearchEntry: DeleteSearchEntry,
    private val deleteAllSearchEntry: DeleteAllSearchEntry,
    private val observeSearchEntry: ObserveSearchEntry,
    private val telemetryManager: TelemetryManager,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository,
    observeVaults: ObserveVaults,
    clock: Clock,
    observeItems: ObserveItems,
    itemSyncStatusRepository: ItemSyncStatusRepository,
    preferencesRepository: UserPreferencesRepository,
    getUserPlan: GetUserPlan,
    appDispatchers: AppDispatchers
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    // Variable to keep track of whether the user has entered the search in this session, so we
    // don't send an EnterSearch event every time they click on the search bar
    private var hasEnteredSearch = false

    private data class FiltersWrapper(
        val vaultSelection: VaultSelectionOption,
        val sortingSelection: SearchSortingType,
        val itemTypeSelection: HomeItemTypeSelection
    )

    private val itemTypeSelectionFlow: MutableStateFlow<HomeItemTypeSelection> =
        MutableStateFlow(HomeItemTypeSelection.AllItems)
    private val shouldScrollToTopFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isInSuggestionsModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isProcessingSearchState: MutableStateFlow<IsProcessingSearchState> =
        MutableStateFlow(IsProcessingSearchState.NotLoading)
    private val hasChangedVaultState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @OptIn(FlowPreview::class)
    private val debouncedSearchQueryState = searchQueryState
        .debounce(DEBOUNCE_TIMEOUT)
        .onStart { emit("") }
        .distinctUntilChanged()

    private val vaultSelectionFlow: Flow<VaultSelectionOption> = homeSearchOptionsRepository
        .observeVaultSelectionOption()
        .distinctUntilChanged()

    init {
        viewModelScope.launch {
            vaultSelectionFlow.collect {
                if (it !is VaultSelectionOption.AllVaults) {
                    hasChangedVaultState.update { true }
                }
            }
        }
    }

    private val shareListWrapperFlow: Flow<ShareListWrapper> = combine(
        vaultSelectionFlow,
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
        homeSearchOptionsRepository.observeVaultSelectionOption()
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
        val actionState: ActionState,
        val syncStatus: ItemSyncStatus
    )

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    private val actionStateFlow: MutableStateFlow<ActionState> =
        MutableStateFlow(ActionState.Unknown)

    private val itemUiModelFlow: Flow<LoadingResult<List<ItemUiModel>>> =
        homeSearchOptionsRepository.observeVaultSelectionOption()
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
        homeSearchOptionsRepository.observeSortingOption()
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
        itemTypeSelectionFlow,
        isInSuggestionsModeState,
        isInSearchModeState
    ) { recentSearchResult, result, itemTypeSelection, isInSuggestionsMode, isInSearchMode ->
        if (isInSuggestionsMode && isInSearchMode) {
            recentSearchResult
        } else {
            result.map { grouped ->
                grouped
                    .map {
                        if (isInSearchMode) {
                            GroupedItemList(it.key, filterByType(it.items, itemTypeSelection))
                        } else {
                            it
                        }
                    }
                    .filter { it.items.isNotEmpty() }
                    .toImmutableList()
            }
        }
    }.flowOn(appDispatchers.default)

    private val refreshingLoadingFlow = combine(
        isRefreshing,
        actionStateFlow,
        itemSyncStatusRepository.observeSyncStatus(),
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

    private val filtersWrapperFlow = combine(
        homeSearchOptionsRepository.observeSearchOptions(),
        itemTypeSelectionFlow
    ) { searchOptions, itemType ->
        shouldScrollToTopFlow.update { true }
        FiltersWrapper(
            vaultSelection = searchOptions.vaultSelectionOption,
            sortingSelection = searchOptions.sortingOption.searchSortingType,
            itemTypeSelection = itemType
        )
    }.distinctUntilChanged()

    val homeUiState = combineN(
        shareListWrapperFlow,
        filtersWrapperFlow,
        resultsFlow,
        searchUiStateFlow,
        refreshingLoadingFlow,
        shouldScrollToTopFlow,
        preferencesRepository.getUseFaviconsPreference(),
        getUserPlan().asLoadingResult(),
        hasChangedVaultState
    ) { shareListWrapper, filtersWrapper, itemsResult, searchUiState, refreshingLoading,
        shouldScrollToTop, useFavicons, userPlan, hasChangedVault ->
        val syncLoading = if (refreshingLoading.syncStatus == ItemSyncStatus.Syncing) {
            IsLoadingState.Loading
        } else {
            IsLoadingState.from(itemsResult is LoadingResult.Loading)
        }

        val (items, isLoading) = when (itemsResult) {
            LoadingResult.Loading -> persistentListOf<GroupedItemList>() to IsLoadingState.Loading
            is LoadingResult.Success -> when (val syncStatus = refreshingLoading.syncStatus) {
                is ItemSyncStatus.Synced -> {

                    val loading = if (itemsResult.data.isEmpty()) {
                        // There are no items emitted yet
                        // Check if SyncStatus says there should be items
                        // If the user has changed vaults, we don't want to check this flag, because
                        // even if the Sync says there are items, there may be none for this vault
                        if (!syncStatus.hasItems) {
                            IsLoadingState.NotLoading
                        } else if (searchUiState.inSearchMode) {
                            when (searchUiState.isProcessingSearch) {
                                IsProcessingSearchState.NotLoading -> syncLoading
                                IsProcessingSearchState.Loading -> IsLoadingState.Loading
                            }
                        } else if (!hasChangedVault) {
                            IsLoadingState.Loading
                        } else {
                            syncLoading
                        }
                    } else {
                        syncLoading
                    }
                    itemsResult.data to loading
                }

                else -> itemsResult.data to syncLoading
            }

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
                homeVaultSelection = filtersWrapper.vaultSelection,
                homeItemTypeSelection = filtersWrapper.itemTypeSelection,
                sortingType = filtersWrapper.sortingSelection,
                canLoadExternalImages = useFavicons.value()
            ),
            searchUiState = searchUiState,
            accountType = AccountType.fromPlan(userPlan)
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = HomeUiState.Loading
        )

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
        itemTypeSelectionFlow.update { HomeItemTypeSelection.AllItems }
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
            applyPendingEvents()
        }.onFailure {
            PassLogger.e(TAG, it, "Apply pending events failed")
            snackbarDispatcher(RefreshError)
        }

        isRefreshing.update { IsRefreshingState.NotRefreshing }
    }

    fun sendItemToTrash(item: ItemUiModel?) = viewModelScope.launch(coroutineExceptionHandler) {
        if (item == null) return@launch

        runCatching { trashItem(shareId = item.shareId, itemId = item.id) }
            .onSuccess {
                when (item.contents) {
                    is ItemContents.Alias -> snackbarDispatcher(AliasMovedToTrash)
                    is ItemContents.Login -> snackbarDispatcher(LoginMovedToTrash)
                    is ItemContents.Note -> snackbarDispatcher(NoteMovedToTrash)
                    is ItemContents.CreditCard -> snackbarDispatcher(HomeSnackbarMessage.CreditCardMovedToTrash)
                    is ItemContents.Unknown -> {}
                }
            }
            .onFailure {
                PassLogger.e(TAG, it, "Trash item failed")
                snackbarDispatcher(HomeSnackbarMessage.MoveToTrashError)
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

    fun setItemTypeSelection(homeItemTypeSelection: HomeItemTypeSelection) {
        itemTypeSelectionFlow.update { homeItemTypeSelection }
        isInSuggestionsModeState.update { false }
    }

    fun setVaultSelection(vaultSelection: VaultSelectionOption) {
        homeSearchOptionsRepository.setVaultSelectionOption(vaultSelection)
    }

    fun restoreActionState() {
        actionStateFlow.update { ActionState.Unknown }
    }

    fun restoreItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }
        runCatching {
            restoreItem.invoke(shareId = shareId, itemId = itemId)
        }.onSuccess {
            actionStateFlow.update { ActionState.Done }
            PassLogger.i(TAG, "Item restored successfully")
        }.onFailure {
            PassLogger.e(TAG, it, "Error restoring item")
            actionStateFlow.update { ActionState.Done }
            snackbarDispatcher(RestoreItemsError)
        }
    }

    fun deleteItem(itemUiModel: ItemUiModel) =
        viewModelScope.launch {
            actionStateFlow.update { ActionState.Loading }
            runCatching {
                deleteItem.invoke(shareId = itemUiModel.shareId, itemId = itemUiModel.id)
            }.onSuccess {
                actionStateFlow.update { ActionState.Done }
                PassLogger.i(TAG, "Item deleted successfully")
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemUiModel.contents)))
            }.onFailure {
                PassLogger.e(TAG, it, "Error deleting item")
                actionStateFlow.update { ActionState.Done }
                snackbarDispatcher(DeleteItemError)
            }
        }

    fun clearTrash() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }

        val deletedItems = homeUiState.value.homeListUiState.items
        runCatching {
            clearTrash.invoke()
        }.onSuccess {
            actionStateFlow.update { ActionState.Done }
            PassLogger.i(TAG, "Trash cleared successfully")
            emitDeletedItems(deletedItems)
        }.onFailure {
            PassLogger.e(TAG, it, "Error clearing trash")
            actionStateFlow.update { ActionState.Done }
            snackbarDispatcher(ClearTrashError)
        }
    }

    fun restoreItems() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }
        runCatching {
            restoreItems.invoke()
        }.onSuccess {
            actionStateFlow.update { ActionState.Done }
            PassLogger.i(TAG, "Items restored successfully")
        }.onFailure {
            PassLogger.e(TAG, it, "Error restoring items")
            actionStateFlow.update { ActionState.Done }
            snackbarDispatcher(RestoreItemsError)
        }
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

    private fun filterByType(
        items: List<ItemUiModel>,
        itemTypeSelection: HomeItemTypeSelection
    ) = items.filter { item ->
        when (itemTypeSelection) {
            HomeItemTypeSelection.AllItems -> true
            HomeItemTypeSelection.Aliases -> item.contents is ItemContents.Alias
            HomeItemTypeSelection.Logins -> item.contents is ItemContents.Login
            HomeItemTypeSelection.Notes -> item.contents is ItemContents.Note
            HomeItemTypeSelection.CreditCards -> item.contents is ItemContents.CreditCard
        }
    }

    private fun emitDeletedItems(items: List<GroupedItemList>) {
        items.forEach { list ->
            list.items.forEach { item ->
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(item.contents)))
            }
        }
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
        private const val MAX_CLIPBOARD_LENGTH = 2500
    }
}

package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.ItemSorter.sortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.sortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.sortByMostRecent
import proton.android.pass.commonui.api.ItemSorter.sortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.sortByTitleDesc
import proton.android.pass.commonui.api.ItemUiFilter.filterByQuery
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.LoginMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ObserveItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RefreshError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trashItem: TrashItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val clipboardManager: ClipboardManager,
    private val applyPendingEvents: ApplyPendingEvents,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getShareById: GetShareById,
    private val restoreItem: RestoreItem,
    private val restoreItems: RestoreItems,
    private val deleteItem: DeleteItem,
    private val clearTrash: ClearTrash,
    clock: Clock,
    observeCurrentUser: ObserveCurrentUser,
    observeItems: ObserveItems
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private data class FiltersWrapper(
        val vaultSelection: HomeVaultSelection,
        val sortingSelection: SortingType,
        val itemTypeSelection: HomeItemTypeSelection
    )

    private val itemTypeSelectionFlow: MutableStateFlow<HomeItemTypeSelection> =
        MutableStateFlow(HomeItemTypeSelection.AllItems)
    private val vaultSelectionFlow: MutableStateFlow<HomeVaultSelection> =
        MutableStateFlow(HomeVaultSelection.AllVaults)

    private val currentUserFlow = observeCurrentUser().filterNotNull()

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isProcessingSearchState: MutableStateFlow<IsProcessingSearchState> =
        MutableStateFlow(IsProcessingSearchState.NotLoading)

    private val selectedShareFlow: Flow<Option<ShareUiModel>> = vaultSelectionFlow
        .mapLatest {
            when (it) {
                HomeVaultSelection.AllVaults -> None
                HomeVaultSelection.Trash -> None
                is HomeVaultSelection.Vault -> shareIdToShare(it.shareId)
            }
        }

    private data class ActionRefreshingWrapper(
        val refreshing: IsRefreshingState,
        val actionState: ActionState
    )

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    private val actionStateFlow: MutableStateFlow<ActionState> =
        MutableStateFlow(ActionState.Unknown)

    private val refreshingLoadingFlow = combine(
        isRefreshing,
        actionStateFlow,
        ::ActionRefreshingWrapper
    )

    private val sortingTypeState: MutableStateFlow<SortingType> =
        MutableStateFlow(SortingType.MostRecent)

    private val itemUiModelFlow: Flow<LoadingResult<List<ItemUiModel>>> = combine(
        vaultSelectionFlow,
        itemTypeSelectionFlow,
    ) { vault, itemType ->
        val (shareSelection, itemState) = when (vault) {
            HomeVaultSelection.AllVaults -> ShareSelection.AllShares to ItemState.Active
            is HomeVaultSelection.Vault -> ShareSelection.Share(vault.shareId) to ItemState.Active
            HomeVaultSelection.Trash -> ShareSelection.AllShares to ItemState.Trashed
        }

        val itemTypeSelection = when (itemType) {
            HomeItemTypeSelection.Aliases -> ItemTypeFilter.Aliases
            HomeItemTypeSelection.AllItems -> ItemTypeFilter.All
            HomeItemTypeSelection.Logins -> ItemTypeFilter.Logins
            HomeItemTypeSelection.Notes -> ItemTypeFilter.Notes
        }

        Triple(shareSelection, itemState, itemTypeSelection)
    }.flatMapLatest {
        observeItems(
            selection = it.first,
            itemState = it.second,
            filter = it.third
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

    private val sortedListItemFlow = combine(
        itemUiModelFlow,
        sortingTypeState
    ) { result, sortingType ->
        when (sortingType) {
            SortingType.TitleAsc -> result.map { list -> list.sortByTitleAsc() }
            SortingType.TitleDesc -> result.map { list -> list.sortByTitleDesc() }
            SortingType.CreationAsc -> result.map { list -> list.sortByCreationAsc() }
            SortingType.CreationDesc -> result.map { list -> list.sortByCreationDesc() }
            SortingType.MostRecent -> result.map { list -> list.sortByMostRecent(clock.now()) }
        }
    }.distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private val textFilterListItemFlow = combine(
        sortedListItemFlow,
        searchQueryState.debounce(DEBOUNCE_TIMEOUT)
    ) { result, searchQuery ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (searchQuery.isNotBlank()) {
            result.map { grouped -> grouped.mapValues { filterByQuery(it.value, searchQuery) } }
        } else {
            result
        }
    }.flowOn(Dispatchers.Default)

    private val resultsFlow: Flow<LoadingResult<ImmutableMap<GroupingKeys, ImmutableList<ItemUiModel>>>> =
        combine(
            textFilterListItemFlow,
            itemTypeSelectionFlow,
            isInSearchModeState
        ) { result, itemTypeSelection, isInSearchMode ->
            result.map { grouped ->
                grouped.mapValues {
                    if (isInSearchMode) {
                        filterByType(it, itemTypeSelection)
                    } else {
                        it.value
                    }
                }
                    .filterValues { it.isNotEmpty() }
                    .mapValues { it.value.toPersistentList() }
                    .toPersistentMap()
            }
        }.flowOn(Dispatchers.Default)

    private val itemTypeCountFlow = textFilterListItemFlow.map { result ->
        when (result) {
            is LoadingResult.Error -> ItemTypeCount(loginCount = 0, aliasCount = 0, noteCount = 0)
            LoadingResult.Loading -> ItemTypeCount(loginCount = 0, aliasCount = 0, noteCount = 0)
            is LoadingResult.Success -> {
                result.data.values.flatten().let { list ->
                    ItemTypeCount(
                        loginCount = list.count { it.itemType is ItemType.Login },
                        aliasCount = list.count { it.itemType is ItemType.Alias },
                        noteCount = list.count { it.itemType is ItemType.Note }
                    )
                }
            }
        }
    }.distinctUntilChanged()

    private val searchUiStateFlow = combine(
        searchQueryState,
        isProcessingSearchState,
        isInSearchModeState,
        itemTypeCountFlow,
        ::SearchUiState
    )

    private val filtersWrapperFlow = combine(
        vaultSelectionFlow,
        sortingTypeState,
        itemTypeSelectionFlow,
        ::FiltersWrapper
    )

    val homeUiState = combine(
        selectedShareFlow,
        filtersWrapperFlow,
        resultsFlow,
        searchUiStateFlow,
        refreshingLoadingFlow,
    ) { selectedShare, filtersWrapper, itemsResult, searchUiState, refreshingLoading ->
        val isLoading = IsLoadingState.from(itemsResult is LoadingResult.Loading)

        val items = when (itemsResult) {
            LoadingResult.Loading -> persistentMapOf()
            is LoadingResult.Success -> itemsResult.data
            is LoadingResult.Error -> {
                PassLogger.e(TAG, itemsResult.exception, "Observe items error")
                snackbarMessageRepository.emitSnackbarMessage(ObserveItemsError)
                persistentMapOf()
            }
        }

        HomeUiState(
            homeListUiState = HomeListUiState(
                isLoading = isLoading,
                isRefreshing = refreshingLoading.refreshing,
                actionState = refreshingLoading.actionState,
                items = items,
                selectedShare = selectedShare,
                homeVaultSelection = filtersWrapper.vaultSelection,
                homeItemTypeSelection = filtersWrapper.itemTypeSelection,
                sortingType = filtersWrapper.sortingSelection
            ),
            searchUiState = searchUiState
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
        isProcessingSearchState.update { IsProcessingSearchState.Loading }
    }

    fun onStopSearching() {
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
        itemTypeSelectionFlow.update { HomeItemTypeSelection.AllItems }
    }

    fun onEnterSearch() {
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
    }

    fun onSortingTypeChanged(sortingType: SortingType) {
        sortingTypeState.update { sortingType }
    }

    fun onRefresh() = viewModelScope.launch(coroutineExceptionHandler) {
        isRefreshing.update { IsRefreshingState.Refreshing }
        applyPendingEvents()
            .onError { t ->
                PassLogger.e(TAG, t, "Apply pending events failed")
                snackbarMessageRepository.emitSnackbarMessage(RefreshError)
            }

        isRefreshing.update { IsRefreshingState.NotRefreshing }
    }

    fun sendItemToTrash(item: ItemUiModel?) = viewModelScope.launch(coroutineExceptionHandler) {
        if (item == null) return@launch

        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            trashItem(userId, item.shareId, item.id)
                .onSuccess {
                    when (item.itemType) {
                        is ItemType.Alias ->
                            snackbarMessageRepository.emitSnackbarMessage(AliasMovedToTrash)
                        is ItemType.Login ->
                            snackbarMessageRepository.emitSnackbarMessage(LoginMovedToTrash)
                        is ItemType.Note ->
                            snackbarMessageRepository.emitSnackbarMessage(NoteMovedToTrash)
                        ItemType.Password -> {}
                    }
                }
        }
    }

    fun copyToClipboard(text: String, homeClipboardType: HomeClipboardType) {
        viewModelScope.launch {
            when (homeClipboardType) {
                HomeClipboardType.Alias -> {
                    withContext(Dispatchers.IO) {
                        clipboardManager.copyToClipboard(text = text)
                    }
                    snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.AliasCopied)
                }
                HomeClipboardType.Note -> {
                    withContext(Dispatchers.IO) {
                        clipboardManager.copyToClipboard(text = text)
                    }
                    snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.NoteCopied)
                }
                HomeClipboardType.Password -> {
                    withContext(Dispatchers.IO) {
                        clipboardManager.copyToClipboard(
                            text = encryptionContextProvider.withEncryptionContext { decrypt(text) },
                            isSecure = true
                        )
                    }
                    snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.PasswordCopied)
                }
                HomeClipboardType.Username -> {
                    withContext(Dispatchers.IO) {
                        clipboardManager.copyToClipboard(text = text)
                    }
                    snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.UsernameCopied)
                }
            }
        }
    }

    fun setItemTypeSelection(homeItemTypeSelection: HomeItemTypeSelection) {
        itemTypeSelectionFlow.update { homeItemTypeSelection }
    }

    fun setVaultSelection(homeVaultSelection: HomeVaultSelection) {
        vaultSelectionFlow.update { homeVaultSelection }
    }

    fun restoreActionState() {
        actionStateFlow.update { ActionState.Unknown }
    }

    fun restoreItem(item: ItemUiModel) = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }
        runCatching {
            restoreItem.invoke(shareId = item.shareId, itemId = item.id)
        }.onSuccess {
            actionStateFlow.update { ActionState.Done }
            PassLogger.i(TAG, "Item restored successfully")
        }.onFailure {
            PassLogger.e(TAG, it, "Error restoring item")
            actionStateFlow.update { ActionState.Done }
            snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.RestoreItemsError)
        }
    }

    fun deleteItem(item: ItemUiModel) = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }
        runCatching {
            deleteItem.invoke(shareId = item.shareId, itemId = item.id)
        }.onSuccess {
            actionStateFlow.update { ActionState.Done }
            PassLogger.i(TAG, "Item deleted successfully")
        }.onFailure {
            PassLogger.e(TAG, it, "Error deleting item")
            actionStateFlow.update { ActionState.Done }
            snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.DeleteItemError)
        }
    }

    fun clearTrash() = viewModelScope.launch {
        actionStateFlow.update { ActionState.Loading }
        runCatching {
            clearTrash.invoke()
        }.onSuccess {
            actionStateFlow.update { ActionState.Done }
            PassLogger.i(TAG, "Trash cleared successfully")
        }.onFailure {
            PassLogger.e(TAG, it, "Error clearing trash")
            actionStateFlow.update { ActionState.Done }
            snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.ClearTrashError)
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
            snackbarMessageRepository.emitSnackbarMessage(HomeSnackbarMessage.RestoreItemsError)
        }
    }

    private suspend fun shareIdToShare(shareId: ShareId): Option<ShareUiModel> {
        val shareResult = getShareById(shareId = shareId)
            .map(Share?::toOption)
            .map {
                it.flatMap { share ->
                    when (val asVault = share.toVault(encryptionContextProvider)) {
                        None -> None
                        is Some -> ShareUiModel.fromVault(asVault.value).toOption()
                    }
                }
            }

        return when (shareResult) {
            LoadingResult.Loading -> None
            is LoadingResult.Error -> {
                PassLogger.w(TAG, shareResult.exception, "Error getting share by id")
                None
            }
            is LoadingResult.Success -> {
                shareResult.data
            }
        }
    }

    private fun filterByType(
        it: Map.Entry<GroupingKeys, List<ItemUiModel>>,
        itemTypeSelection: HomeItemTypeSelection
    ) = it.value.filter { item ->
        when (itemTypeSelection) {
            HomeItemTypeSelection.AllItems -> true
            HomeItemTypeSelection.Aliases -> item.itemType is ItemType.Alias
            HomeItemTypeSelection.Logins -> item.itemType is ItemType.Login
            HomeItemTypeSelection.Notes -> item.itemType is ItemType.Note
        }
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
    }
}

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
import proton.android.pass.commonui.api.ItemUiFilter
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.LoginMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ObserveItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RefreshError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ItemType
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
    clock: Clock,
    observeCurrentUser: ObserveCurrentUser,
    observeActiveItems: ObserveActiveItems
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val itemTypeSelectionFlow: MutableStateFlow<HomeItemTypeSelection> =
        MutableStateFlow(HomeItemTypeSelection.AllItems)
    private val vaultSelectionFlow: MutableStateFlow<HomeVaultSelection> =
        MutableStateFlow(HomeVaultSelection.AllVaults)

    private val currentUserFlow = observeCurrentUser().filterNotNull()

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isProcessingSearchState: MutableStateFlow<IsProcessingSearchState> =
        MutableStateFlow(IsProcessingSearchState.NotLoading)

    private val searchWrapperWrapper = combine(
        searchQueryState,
        isInSearchModeState,
        isProcessingSearchState
    ) { searchQuery, isInSearchMode, isProcessingSearch ->
        SearchWrapper(searchQuery, isInSearchMode, isProcessingSearch)
    }

    private val selectedShareFlow: Flow<Option<ShareUiModel>> = vaultSelectionFlow
        .mapLatest {
            when (it) {
                HomeVaultSelection.AllVaults -> None
                HomeVaultSelection.Trash -> None
                is HomeVaultSelection.Vault -> {
                    shareIdToShare(it.shareId)
                }
            }
        }

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    private val sortingTypeState: MutableStateFlow<SortingType> =
        MutableStateFlow(SortingType.MostRecent)

    private val activeItemUIModelFlow: Flow<LoadingResult<List<ItemUiModel>>> = vaultSelectionFlow
        .flatMapLatest { selectedVault ->
            val shareSelection = when (selectedVault) {
                HomeVaultSelection.AllVaults -> ShareSelection.AllShares
                is HomeVaultSelection.Vault -> ShareSelection.Share(selectedVault.shareId)
                HomeVaultSelection.Trash -> ShareSelection.AllShares
            }
            observeActiveItems(shareSelection = shareSelection)
                .asResultWithoutLoading()
                .map { itemResult ->
                    itemResult.map { list ->
                        encryptionContextProvider.withEncryptionContext {
                            list.map { it.toUiModel(this@withEncryptionContext) }
                        }
                    }
                }
                .distinctUntilChanged()
        }

    private val sortedListItemFlow: Flow<LoadingResult<Map<GroupingKeys, List<ItemUiModel>>>> =
        combine(
            activeItemUIModelFlow,
            sortingTypeState
        ) { result, sortingType ->
            when (sortingType) {
                SortingType.TitleAsc -> result.map { list -> list.sortByTitleAsc() }
                SortingType.TitleDesc -> result.map { list -> list.sortByTitleDesc() }
                SortingType.CreationAsc -> result.map { list -> list.sortByCreationAsc() }
                SortingType.CreationDesc -> result.map { list -> list.sortByCreationDesc() }
                SortingType.MostRecent -> result.map { list -> list.sortByMostRecent(clock.now()) }
            }
        }
            .distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private val resultsFlow: Flow<LoadingResult<ImmutableMap<GroupingKeys, ImmutableList<ItemUiModel>>>> =
        combine(
            sortedListItemFlow,
            searchQueryState.debounce(DEBOUNCE_TIMEOUT),
            itemTypeSelectionFlow
        ) { result, searchQuery, itemTypeSelection ->
            isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
            if (searchQuery.isNotBlank()) {
                result.map { grouped ->
                    grouped.mapValues {
                        ItemUiFilter.filterByQuery(it.value, searchQuery).toPersistentList()
                    }
                        .filterValues { it.isNotEmpty() }
                        .toPersistentMap()
                }
            } else {
                result.map { items ->
                    items
                        .mapValues {
                            it.value.filter { item ->
                                when (itemTypeSelection) {
                                    HomeItemTypeSelection.AllItems -> true
                                    HomeItemTypeSelection.Aliases -> item.itemType is ItemType.Alias
                                    HomeItemTypeSelection.Logins -> item.itemType is ItemType.Login
                                    HomeItemTypeSelection.Notes -> item.itemType is ItemType.Note
                                }
                            }.toPersistentList()
                        }
                        .filterValues { it.isNotEmpty() }
                        .toPersistentMap()
                }
            }
        }.flowOn(Dispatchers.Default)

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean,
        val isProcessingSearch: IsProcessingSearchState
    )

    val homeUiState = combine(
        selectedShareFlow,
        resultsFlow,
        searchWrapperWrapper,
        isRefreshing,
        sortingTypeState
    ) { selectedShare, itemsResult, searchWrapper, refreshing, sortingType ->
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
                isRefreshing = refreshing,
                items = items,
                selectedShare = selectedShare,
                sortingType = sortingType
            ),
            searchUiState = SearchUiState(
                searchQuery = searchWrapper.searchQuery,
                inSearchMode = searchWrapper.isInSearchMode,
                isProcessingSearch = searchWrapper.isProcessingSearch
            )
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

    private suspend fun shareIdToShare(shareId: ShareId): Option<ShareUiModel> {
        val shareResult = getShareById(shareId = shareId)
            .map {
                if (it == null) {
                    None
                } else {
                    when (val asVault = it.toVault(encryptionContextProvider)) {
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


    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
    }
}

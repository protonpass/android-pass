package proton.android.pass.featurehome.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.ItemUiFilter
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasCopied
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.AliasMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.LoginMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteCopied
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.NoteMovedToTrash
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.ObserveItemsError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.PasswordCopied
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.RefreshError
import proton.android.pass.featurehome.impl.HomeSnackbarMessage.UsernameCopied
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.ItemType
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

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    private val sortingTypeState: MutableStateFlow<SortingType> =
        MutableStateFlow(SortingType.ByModificationDate)

    private val activeItemUIModelFlow: Flow<Result<List<ItemUiModel>>> = vaultSelectionFlow
        .flatMapLatest { selectedVault ->
            val shareSelection = when (selectedVault) {
                HomeVaultSelection.AllVaults -> ShareSelection.AllShares
                is HomeVaultSelection.Vault -> ShareSelection.Share(selectedVault.shareId)
            }
            observeActiveItems(shareSelection = shareSelection)
                .map { itemResult ->
                    itemResult.map { list ->
                        encryptionContextProvider.withEncryptionContext {
                            list.map { it.toUiModel(this@withEncryptionContext) }
                        }
                    }
                }
                .distinctUntilChanged()
        }

    private val sortedListItemFlow: Flow<Result<List<ItemUiModel>>> = combine(
        activeItemUIModelFlow,
        sortingTypeState
    ) { result, sortingType ->
        when (sortingType) {
            SortingType.ByName -> result.map { list -> list.sortByTitle() }
            SortingType.ByItemType -> result.map { list -> list.sortByItemType() }
            SortingType.ByModificationDate -> result.map { list -> list.sortByModificationTime() }
        }
    }
        .distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private val resultsFlow: Flow<Result<List<ItemUiModel>>> = combine(
        sortedListItemFlow,
        searchQueryState.debounce(DEBOUNCE_TIMEOUT),
        itemTypeSelectionFlow
    ) { result, searchQuery, itemTypeSelection ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (searchQuery.isNotBlank()) {
            result.map { ItemUiFilter.filterByQuery(it, searchQuery) }
        } else {
            result.map { items ->
                items.filter {
                    when (itemTypeSelection) {
                        HomeItemTypeSelection.AllItems -> true
                        HomeItemTypeSelection.Aliases -> it.itemType is ItemType.Alias
                        HomeItemTypeSelection.Logins -> it.itemType is ItemType.Login
                        HomeItemTypeSelection.Notes -> it.itemType is ItemType.Note
                    }
                }
            }
        }
    }.flowOn(Dispatchers.Default)

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean,
        val isProcessingSearch: IsProcessingSearchState
    )

    val homeUiState = combine(
        vaultSelectionFlow,
        resultsFlow,
        searchWrapperWrapper,
        isRefreshing,
        sortingTypeState
    ) { vaultSelection, itemsResult, searchWrapper, refreshing, sortingType ->
        val isLoading = IsLoadingState.from(itemsResult is Result.Loading)

        val items = when (itemsResult) {
            Result.Loading -> emptyList()
            is Result.Success -> itemsResult.data
            is Result.Error -> {
                val defaultMessage = "Observe items error"
                PassLogger.e(
                    TAG,
                    itemsResult.exception ?: Exception(defaultMessage),
                    defaultMessage
                )
                snackbarMessageRepository.emitSnackbarMessage(ObserveItemsError)
                emptyList()
            }
        }

        val selectedShare = when (vaultSelection) {
            HomeVaultSelection.AllVaults -> None
            is HomeVaultSelection.Vault -> vaultSelection.shareId.toOption()
        }

        HomeUiState(
            homeListUiState = HomeListUiState(
                isLoading = isLoading,
                isRefreshing = refreshing,
                items = items.toImmutableList(),
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
        val userId = currentUserFlow.firstOrNull()?.userId
        val share = homeUiState.value.homeListUiState.selectedShare
        if (userId != null && share is Some) {
            isRefreshing.update { IsRefreshingState.Refreshing }
            runCatching {
                applyPendingEvents(userId, share.value)
            }.onFailure {
                PassLogger.e(TAG, it, "Error in refresh")
                snackbarMessageRepository.emitSnackbarMessage(RefreshError)
            }

            isRefreshing.update { IsRefreshingState.NotRefreshing }
        }
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
        when (homeClipboardType) {
            HomeClipboardType.Alias -> {
                clipboardManager.copyToClipboard(text = text)
                viewModelScope.launch {
                    snackbarMessageRepository.emitSnackbarMessage(AliasCopied)
                }
            }
            HomeClipboardType.Note -> {
                clipboardManager.copyToClipboard(text = text)
                viewModelScope.launch {
                    snackbarMessageRepository.emitSnackbarMessage(NoteCopied)
                }
            }
            HomeClipboardType.Password -> {
                encryptionContextProvider.withEncryptionContext {
                    clipboardManager.copyToClipboard(
                        text = decrypt(text),
                        isSecure = true
                    )
                }
                viewModelScope.launch {
                    snackbarMessageRepository.emitSnackbarMessage(PasswordCopied)
                }
            }
            HomeClipboardType.Username -> {
                clipboardManager.copyToClipboard(text = text)
                viewModelScope.launch {
                    snackbarMessageRepository.emitSnackbarMessage(UsernameCopied)
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

    private fun List<ItemUiModel>.sortByTitle() = sortedBy { it.name.lowercase() }

    private fun List<ItemUiModel>.sortByItemType() =
        groupBy { it.itemType.toWeightedInt() }
            .toSortedMap()
            .map { it.value }
            .flatten()

    private fun List<ItemUiModel>.sortByModificationTime() =
        sortedBy { it.modificationTime }.reversed()

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
    }
}

package me.proton.pass.presentation.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.clipboard.api.ClipboardManager
import me.proton.android.pass.data.api.usecases.ApplyPendingEvents
import me.proton.android.pass.data.api.usecases.ObserveActiveItems
import me.proton.android.pass.data.api.usecases.ObserveActiveShare
import me.proton.android.pass.data.api.usecases.ObserveCurrentUser
import me.proton.android.pass.data.api.usecases.TrashItem
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.map
import me.proton.pass.domain.Item
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.extension.toUiModel
import me.proton.pass.presentation.home.HomeSnackbarMessage.AliasCopied
import me.proton.pass.presentation.home.HomeSnackbarMessage.NoteCopied
import me.proton.pass.presentation.home.HomeSnackbarMessage.ObserveItemsError
import me.proton.pass.presentation.home.HomeSnackbarMessage.ObserveShareError
import me.proton.pass.presentation.home.HomeSnackbarMessage.PasswordCopied
import me.proton.pass.presentation.home.HomeSnackbarMessage.RefreshError
import me.proton.pass.presentation.home.HomeSnackbarMessage.UsernameCopied
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState
import me.proton.pass.search.ItemFilter
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val trashItem: TrashItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val clipboardManager: ClipboardManager,
    private val applyPendingEvents: ApplyPendingEvents,
    observeCurrentUser: ObserveCurrentUser,
    observeActiveShare: ObserveActiveShare,
    observeActiveItems: ObserveActiveItems,
    itemFilter: ItemFilter
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val currentUserFlow = observeCurrentUser().filterNotNull()

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isAppLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val searchWrapperWrapper = combine(
        searchQueryState,
        isInSearchModeState
    ) { searchQuery, isInSearchMode -> SearchWrapper(searchQuery, isInSearchMode) }

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    private val sortingTypeState: MutableStateFlow<SortingType> =
        MutableStateFlow(SortingType.ByName)

    private val sortedListItemFlow: Flow<Result<List<Item>>> = combine(
        observeActiveItems(),
        sortingTypeState
    ) { result, sortingType ->
        when (sortingType) {
            SortingType.ByName -> result.map { list -> list.sortByTitle(keyStoreCrypto) }
            SortingType.ByItemType -> result.map { list -> list.sortByItemType(keyStoreCrypto) }
        }
    }
        .distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private val resultsFlow: Flow<Result<List<ItemUiModel>>> = combine(
        sortedListItemFlow,
        searchQueryState.debounce(DEBOUNCE_TIMEOUT)
    ) { sortedList, searchQuery ->
        itemFilter.filterByQuery(sortedList, searchQuery)
    }.mapLatest { result: Result<List<Item>> ->
        result.map { list ->
            list.map { it.toUiModel(keyStoreCrypto) }
        }
    }.flowOn(Dispatchers.Default)

    private val refreshingState: Flow<IsRefreshingState> = combine(
        isRefreshing,
        isAppLoadingState
    ) { refreshing, appLoading ->
        when (refreshing) {
            IsRefreshingState.Refreshing -> IsRefreshingState.Refreshing
            IsRefreshingState.NotRefreshing -> when (appLoading) {
                IsLoadingState.Loading -> IsRefreshingState.Refreshing
                IsLoadingState.NotLoading -> IsRefreshingState.NotRefreshing
            }
        }
    }

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean
    )

    val homeUiState: StateFlow<HomeUiState> = combine(
        observeActiveShare(),
        resultsFlow,
        searchWrapperWrapper,
        refreshingState,
        sortingTypeState
    ) { shareIdResult, itemsResult, searchWrapper, isRefreshing, sortingType ->
        val isLoading = IsLoadingState.from(
            shareIdResult is Result.Loading || itemsResult is Result.Loading
        )

        val items = when (itemsResult) {
            Result.Loading -> emptyList()
            is Result.Success -> itemsResult.data
            is Result.Error -> {
                val defaultMessage = "Observe items error"
                PassLogger.i(
                    TAG,
                    itemsResult.exception ?: Exception(defaultMessage),
                    defaultMessage
                )
                snackbarMessageRepository.emitSnackbarMessage(ObserveItemsError)
                emptyList()
            }
        }

        val selectedShare = when (shareIdResult) {
            Result.Loading -> None
            is Result.Success -> Option.fromNullable(shareIdResult.data)
            is Result.Error -> {
                val defaultMessage = "Observe active share error"
                PassLogger.i(
                    TAG,
                    shareIdResult.exception ?: Exception(defaultMessage),
                    defaultMessage
                )
                snackbarMessageRepository.emitSnackbarMessage(ObserveShareError)
                None
            }
        }

        HomeUiState(
            homeListUiState = HomeListUiState(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                items = items,
                selectedShare = selectedShare,
                sortingType = sortingType
            ),
            searchUiState = SearchUiState(
                searchQuery = searchWrapper.searchQuery,
                inSearchMode = searchWrapper.isInSearchMode
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onSearchQueryChange(query: String) {
        if (query.contains("\n")) return

        searchQueryState.value = query
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
                PassLogger.i(TAG, it, "Error in refresh")
                snackbarMessageRepository.emitSnackbarMessage(RefreshError)
            }

            isRefreshing.update { IsRefreshingState.NotRefreshing }
        }
    }

    fun sendItemToTrash(item: ItemUiModel?) = viewModelScope.launch(coroutineExceptionHandler) {
        if (item == null) return@launch

        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            trashItem.invoke(userId, item.shareId, item.id)
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
                clipboardManager.copyToClipboard(
                    text = keyStoreCrypto.decrypt(text),
                    isSecure = true
                )
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

    fun setAppLoadingState(appLoadingState: IsLoadingState) {
        val mapped = when (appLoadingState) {
            IsLoadingState.NotLoading -> IsRefreshingState.NotRefreshing
            IsLoadingState.Loading -> IsRefreshingState.Refreshing
        }
        isRefreshing.update { mapped }
    }

    private fun List<Item>.sortByTitle(crypto: KeyStoreCrypto) =
        sortedBy { it.title.decrypt(crypto).lowercase() }

    private fun List<Item>.sortByItemType(crypto: KeyStoreCrypto) =
        groupBy { it.itemType.toWeightedInt() }
            .toSortedMap()
            .map { it.value.sortByTitle(crypto) }
            .flatten()

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "HomeViewModel"
    }
}

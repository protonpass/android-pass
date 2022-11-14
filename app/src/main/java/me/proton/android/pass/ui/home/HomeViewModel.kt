package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.ui.home.HomeSnackbarMessage.ObserveItemsError
import me.proton.android.pass.ui.home.HomeSnackbarMessage.ObserveShareError
import me.proton.android.pass.ui.home.HomeSnackbarMessage.RefreshError
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.common.api.onError
import me.proton.pass.domain.Item
import me.proton.pass.domain.usecases.ObserveActiveShare
import me.proton.pass.domain.usecases.ObserveCurrentUser
import me.proton.pass.domain.usecases.RefreshContent
import me.proton.pass.domain.usecases.TrashItem
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.extension.toUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState
import me.proton.pass.search.SearchItems
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val trashItem: TrashItem,
    private val searchItems: SearchItems,
    private val refreshContent: RefreshContent,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    observeCurrentUser: ObserveCurrentUser,
    observeActiveShare: ObserveActiveShare
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val currentUserFlow = observeCurrentUser().filterNotNull()

    private val listItems: Flow<Result<List<ItemUiModel>>> = searchItems.observeResults()
        .mapLatest { result: Result<List<Item>> ->
            result.map { list ->
                list.map { it.toUiModel(keyStoreCrypto) }
            }
        }

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val searchWrapperWrapper = combine(
        searchQueryState,
        isInSearchModeState
    ) { searchQuery, isInSearchMode -> SearchWrapper(searchQuery, isInSearchMode) }

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean
    )

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)

    val homeUiState: StateFlow<HomeUiState> = combine(
        observeActiveShare(),
        listItems,
        searchWrapperWrapper,
        isRefreshing
    ) { shareIdResult, itemsResult, searchWrapper, isRefreshing ->
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
                selectedShare = selectedShare
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
        searchItems.updateQuery(query)
    }

    fun onStopSearching() {
        searchItems.clearSearch()
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
    }

    fun onEnterSearch() {
        searchItems.clearSearch()
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
    }

    fun onRefresh() = viewModelScope.launch(coroutineExceptionHandler) {
        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            isRefreshing.update { IsRefreshingState.Refreshing }
            refreshContent(userId)
                .onError {
                    val message = "Error in refresh"
                    PassLogger.i(TAG, it ?: Exception(message), message)
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

    companion object {
        private const val TAG = "HomeViewModel"
    }
}

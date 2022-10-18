package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import me.proton.android.pass.extension.toUiModel
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.map
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.usecases.ObserveActiveShare
import me.proton.core.pass.domain.usecases.ObserveCurrentUser
import me.proton.core.pass.domain.usecases.RefreshContent
import me.proton.core.pass.domain.usecases.TrashItem
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.search.SearchItems
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val trashItem: TrashItem,
    private val searchItems: SearchItems,
    private val refreshContent: RefreshContent,
    observeCurrentUser: ObserveCurrentUser,
    observeActiveShare: ObserveActiveShare
) : ViewModel() {

    private val currentUserFlow = observeCurrentUser().filterNotNull()

    private val listItems: Flow<Result<List<ItemUiModel>>> = searchItems.observeResults()
        .mapLatest { result: Result<List<Item>> ->
            result.map { list ->
                list.map { it.toUiModel(cryptoContext) }
            }
        }

    private val searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    private val inSearchMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isRefreshing: MutableStateFlow<IsLoadingState> = MutableStateFlow(IsLoadingState.NotLoading)

    val homeUiState: StateFlow<HomeUiState> = combine(
        observeActiveShare(),
        listItems,
        searchQuery,
        inSearchMode,
        isRefreshing
    ) { shareIdResult, itemsResult, searchQuery, inSearchMode, isRefreshing ->
        val isLoading = if (shareIdResult is Result.Loading || itemsResult is Result.Loading) {
            IsLoadingState.Loading
        } else {
            IsLoadingState.NotLoading
        }
        val items = (itemsResult as? Result.Success)?.data ?: emptyList()
        val selectedShare = (shareIdResult as? Result.Success)?.data
        val errorMessage = (shareIdResult as? Result.Error)?.exception?.message
            ?: (itemsResult as? Result.Error)?.exception?.message

        HomeUiState(
            homeListUiState = HomeListUiState(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                items = items,
                selectedShare = selectedShare,
                errorMessage = errorMessage
            ),
            searchUiState = SearchUiState(
                searchQuery = searchQuery,
                inSearchMode = inSearchMode
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(
                homeListUiState = HomeListUiState(
                    isLoading = IsLoadingState.Loading,
                    isRefreshing = IsLoadingState.NotLoading,
                    items = emptyList(),
                    selectedShare = null,
                    errorMessage = null
                ),
                searchUiState = SearchUiState(
                    searchQuery = "",
                    inSearchMode = false
                )
            )
        )

    fun onSearchQueryChange(query: String) = viewModelScope.launch {
        if (query.contains("\n")) return@launch

        searchQuery.value = query
        searchItems.updateQuery(query)
    }

    fun onStopSearching() = viewModelScope.launch {
        searchItems.clearSearch()
        searchQuery.update { "" }
        inSearchMode.update { false }
    }

    fun onEnterSearch() = viewModelScope.launch {
        searchItems.clearSearch()
        searchQuery.update { "" }
        inSearchMode.update { true }
    }

    fun onRefresh() = viewModelScope.launch {
        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            isRefreshing.update { IsLoadingState.Loading }
            refreshContent(userId)
            isRefreshing.update { IsLoadingState.NotLoading }
        }
    }

    fun sendItemToTrash(item: ItemUiModel?) = viewModelScope.launch {
        if (item == null) return@launch

        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            trashItem.invoke(userId, item.shareId, item.id)
        }
    }
}

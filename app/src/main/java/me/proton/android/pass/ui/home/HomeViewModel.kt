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
    private val isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val homeUiState: StateFlow<HomeUiState> = combine(
        observeActiveShare(),
        listItems,
        searchQuery,
        inSearchMode,
        isRefreshing
    ) { shareIdResult, itemsResult, searchQuery, inSearchMode, isRefreshing ->
        if (isRefreshing) return@combine HomeUiState.Loading
        val share = when (shareIdResult) {
            is Result.Success -> shareIdResult.data
            is Result.Error -> return@combine HomeUiState.Error("Could not load share id")
            Result.Loading -> return@combine HomeUiState.Loading
        }
        val itemList: List<ItemUiModel> = when (itemsResult) {
            is Result.Success -> itemsResult.data
            is Result.Error -> return@combine HomeUiState.Error("Could not load item list")
            Result.Loading -> return@combine HomeUiState.Loading
        }
        HomeUiState.Content(itemList, share, searchQuery, inSearchMode)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun onSearchQueryChange(query: String) = viewModelScope.launch {
        if (query.contains("\n")) return@launch

        searchQuery.value = query
        searchItems.updateQuery(query)
    }

    fun onStopSearching() = viewModelScope.launch {
        searchItems.clearSearch()
        searchQuery.value = ""
        inSearchMode.value = false
    }

    fun onEnterSearch() = viewModelScope.launch {
        searchItems.clearSearch()
        searchQuery.value = ""
        inSearchMode.value = true
    }

    fun onRefresh() = viewModelScope.launch {
        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            isRefreshing.update { true }
            refreshContent(userId)
            isRefreshing.update { false }
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

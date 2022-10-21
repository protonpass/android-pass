package me.proton.core.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Immutable
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsRefreshingState

@Immutable
data class SelectItemUiState(
    val listUiState: SelectItemListUiState,
    val searchUiState: SearchUiState
) {
    companion object {
        val Loading = SelectItemUiState(
            listUiState = SelectItemListUiState.Loading,
            searchUiState = SearchUiState.Initial
        )
    }
}

data class SelectItemListUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val itemClickedEvent: ItemClickedEvent,
    val items: List<ItemUiModel>
) {
    companion object {
        val Loading = SelectItemListUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            itemClickedEvent = ItemClickedEvent.None,
            items = emptyList()
        )
    }
}

@Immutable
data class SearchUiState(
    val searchQuery: String,
    val inSearchMode: Boolean
) {
    companion object {
        val Initial = SearchUiState(
            searchQuery = "",
            inSearchMode = false
        )
    }
}

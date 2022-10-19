package me.proton.core.pass.autofill.ui.autofill.select

import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsRefreshingState

data class SelectItemUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val itemClickedEvent: ItemClickedEvent,
    val items: List<ItemUiModel>
) {
    companion object {
        val Loading = SelectItemUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            itemClickedEvent = ItemClickedEvent.None,
            items = emptyList()
        )
    }
}

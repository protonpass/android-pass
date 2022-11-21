package me.proton.pass.presentation.trash

import androidx.compose.runtime.Immutable
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState

@Immutable
data class TrashUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val items: List<ItemUiModel>
) {
    companion object {
        val Loading = TrashUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = emptyList()
        )
    }
}

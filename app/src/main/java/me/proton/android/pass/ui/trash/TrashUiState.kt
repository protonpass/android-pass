package me.proton.android.pass.ui.trash

import androidx.compose.runtime.Immutable
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsRefreshingState

@Immutable
data class TrashUiState(
    val isLoading: IsLoadingState,
    val isRefreshing: IsRefreshingState,
    val items: List<ItemUiModel>,
    val errorMessage: Option<String>
) {
    companion object {
        val Loading = TrashUiState(
            isLoading = IsLoadingState.Loading,
            isRefreshing = IsRefreshingState.NotRefreshing,
            items = emptyList(),
            errorMessage = None
        )
    }
}

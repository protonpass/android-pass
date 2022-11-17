package me.proton.pass.presentation.detail

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.Item
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsSentToTrashState

@Immutable
data class ItemDetailScreenUiState(
    val model: Option<ItemModelUiState>,
    val isLoading: IsLoadingState,
    val isSentToTrash: IsSentToTrashState
) {
    companion object {
        val Loading = ItemDetailScreenUiState(
            isLoading = IsLoadingState.Loading,
            isSentToTrash = IsSentToTrashState.NotSent,
            model = None
        )
    }
}

@Immutable
data class ItemModelUiState(
    val name: String,
    val item: Item
)

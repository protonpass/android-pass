package me.proton.android.pass.ui.detail

import androidx.compose.runtime.Immutable
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.domain.Item
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsSentToTrashState

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

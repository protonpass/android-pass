package me.proton.android.pass.ui.trash

import androidx.compose.runtime.Immutable
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel

@Immutable
sealed class TrashUiState {
    object Loading: TrashUiState()
    data class Content(
        val items: List<ItemUiModel>,
        val selectedShare: ShareId? = null
    ): TrashUiState()
    data class Error(val message: String): TrashUiState()
}

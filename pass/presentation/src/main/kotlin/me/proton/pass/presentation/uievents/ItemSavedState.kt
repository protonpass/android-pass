package me.proton.pass.presentation.uievents

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ItemId
import me.proton.pass.presentation.components.model.ItemUiModel

@Stable
sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(
        val itemId: ItemId,
        val item: ItemUiModel
    ) : ItemSavedState
}

package me.proton.pass.presentation.uievents

import androidx.compose.runtime.Stable
import me.proton.pass.domain.ItemId

@Stable
sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(val itemId: ItemId) : ItemSavedState
}

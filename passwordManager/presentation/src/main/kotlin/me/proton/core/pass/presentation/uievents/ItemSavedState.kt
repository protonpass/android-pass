package me.proton.core.pass.presentation.uievents

import me.proton.core.pass.domain.ItemId

sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(val itemId: ItemId) : ItemSavedState
}

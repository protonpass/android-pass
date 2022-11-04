package me.proton.pass.presentation.uievents

import me.proton.pass.domain.ItemId

sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(val itemId: ItemId) : ItemSavedState
}

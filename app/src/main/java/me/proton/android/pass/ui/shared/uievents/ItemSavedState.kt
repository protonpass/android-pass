package me.proton.android.pass.ui.shared.uievents

import me.proton.core.pass.domain.ItemId

sealed interface ItemSavedState {
    object Unknown : ItemSavedState
    data class Success(val itemId: ItemId) : ItemSavedState
}

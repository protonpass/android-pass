package me.proton.pass.presentation.uievents

sealed interface ItemDeletedState {
    object Unknown : ItemDeletedState
    object Deleted : ItemDeletedState
}

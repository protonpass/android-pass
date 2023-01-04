package me.proton.pass.presentation.create.alias

sealed interface ItemDeletedState {
    object Unknown : ItemDeletedState
    object Deleted : ItemDeletedState
}

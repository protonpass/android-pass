package me.proton.android.pass.featurecreateitem.impl.alias

sealed interface ItemDeletedState {
    object Unknown : ItemDeletedState
    object Deleted : ItemDeletedState
}

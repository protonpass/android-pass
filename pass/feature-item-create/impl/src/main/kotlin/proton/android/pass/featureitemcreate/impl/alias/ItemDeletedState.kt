package proton.android.pass.featureitemcreate.impl.alias

sealed interface ItemDeletedState {
    object Unknown : ItemDeletedState
    object Deleted : ItemDeletedState
}

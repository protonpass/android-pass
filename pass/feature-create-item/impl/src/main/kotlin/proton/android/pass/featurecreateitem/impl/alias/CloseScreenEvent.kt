package proton.android.pass.featurecreateitem.impl.alias

sealed interface CloseScreenEvent {
    object NotClose : CloseScreenEvent
    object Close : CloseScreenEvent
}

package proton.android.pass.featureitemcreate.impl.alias

sealed interface CloseScreenEvent {
    object NotClose : CloseScreenEvent
    object Close : CloseScreenEvent
}

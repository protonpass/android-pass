package me.proton.pass.presentation.create.alias

sealed interface CloseScreenEvent {
    object NotClose : CloseScreenEvent
    object Close : CloseScreenEvent
}

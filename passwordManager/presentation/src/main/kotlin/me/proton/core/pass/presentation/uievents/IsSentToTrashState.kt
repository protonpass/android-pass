package me.proton.core.pass.presentation.uievents

sealed interface IsSentToTrashState {
    object Sent : IsSentToTrashState
    object NotSent : IsSentToTrashState
}

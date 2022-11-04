package me.proton.pass.presentation.uievents

sealed interface IsSentToTrashState {
    object Sent : IsSentToTrashState
    object NotSent : IsSentToTrashState
}

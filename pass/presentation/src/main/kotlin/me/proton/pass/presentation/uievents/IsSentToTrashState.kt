package me.proton.pass.presentation.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsSentToTrashState {
    object Sent : IsSentToTrashState
    object NotSent : IsSentToTrashState
}

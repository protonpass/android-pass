package me.proton.pass.presentation.create

import androidx.compose.runtime.Stable

@Stable
sealed interface IsSentToTrashState {
    object Sent : IsSentToTrashState
    object NotSent : IsSentToTrashState
}

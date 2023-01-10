package proton.android.pass.featurecreateitem.impl

import androidx.compose.runtime.Stable

@Stable
sealed interface IsSentToTrashState {
    object Sent : IsSentToTrashState
    object NotSent : IsSentToTrashState
}

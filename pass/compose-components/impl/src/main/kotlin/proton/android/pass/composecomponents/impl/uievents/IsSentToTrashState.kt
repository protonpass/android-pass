package proton.android.pass.composecomponents.impl.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsSentToTrashState {
    object Sent : IsSentToTrashState
    object NotSent : IsSentToTrashState

    fun value(): Boolean = when (this) {
        Sent -> true
        NotSent -> false
    }

    companion object {
        fun from(value: Boolean): IsSentToTrashState = if (value) Sent else NotSent
    }
}

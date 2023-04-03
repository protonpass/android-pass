package proton.android.pass.composecomponents.impl.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsPermanentlyDeletedState {
    object Deleted : IsPermanentlyDeletedState
    object NotDeleted : IsPermanentlyDeletedState

    fun value(): Boolean = when (this) {
        Deleted -> true
        NotDeleted -> false
    }

    companion object {
        fun from(value: Boolean): IsPermanentlyDeletedState = if (value) Deleted else NotDeleted
    }
}

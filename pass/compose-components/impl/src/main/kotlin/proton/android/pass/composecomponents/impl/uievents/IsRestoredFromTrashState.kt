package proton.android.pass.composecomponents.impl.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsRestoredFromTrashState {
    object Restored : IsRestoredFromTrashState
    object NotRestored : IsRestoredFromTrashState

    fun value(): Boolean = when (this) {
        Restored -> true
        NotRestored -> false
    }

    companion object {
        fun from(value: Boolean): IsRestoredFromTrashState = if (value) Restored else NotRestored
    }
}

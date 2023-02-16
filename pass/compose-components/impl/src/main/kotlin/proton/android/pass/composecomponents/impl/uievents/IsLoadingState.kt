package proton.android.pass.composecomponents.impl.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsLoadingState {
    object Loading : IsLoadingState
    object NotLoading : IsLoadingState

    fun value(): Boolean = when (this) {
        Loading -> true
        NotLoading -> false
    }

    companion object {
        fun from(value: Boolean): IsLoadingState = if (value) Loading else NotLoading
    }
}

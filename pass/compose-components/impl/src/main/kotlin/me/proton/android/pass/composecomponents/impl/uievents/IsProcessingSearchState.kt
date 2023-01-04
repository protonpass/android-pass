package me.proton.android.pass.composecomponents.impl.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsProcessingSearchState {
    object Loading : IsProcessingSearchState
    object NotLoading : IsProcessingSearchState

    companion object {
        fun from(value: Boolean): IsProcessingSearchState = if (value) Loading else NotLoading
    }
}

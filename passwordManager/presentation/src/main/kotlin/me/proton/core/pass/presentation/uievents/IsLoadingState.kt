package me.proton.core.pass.presentation.uievents

sealed interface IsLoadingState {
    object Loading : IsLoadingState
    object NotLoading : IsLoadingState

    companion object {
        fun from(value: Boolean): IsLoadingState = if (value) Loading else NotLoading
    }
}

package me.proton.core.pass.presentation.uievents

sealed interface IsLoadingState {
    object Loading : IsLoadingState
    object NotLoading : IsLoadingState
}

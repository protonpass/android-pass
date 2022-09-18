package me.proton.android.pass.ui.shared.uievents

sealed interface IsLoadingState {
    object Loading : IsLoadingState
    object NotLoading : IsLoadingState
}

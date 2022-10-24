package me.proton.pass.presentation.uievents

sealed interface IsRefreshingState {
    object Refreshing : IsRefreshingState
    object NotRefreshing : IsRefreshingState
}

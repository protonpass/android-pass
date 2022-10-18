package me.proton.core.pass.presentation.uievents

sealed interface IsRefreshingState {
    object Refreshing : IsRefreshingState
    object NotRefreshing : IsRefreshingState
}

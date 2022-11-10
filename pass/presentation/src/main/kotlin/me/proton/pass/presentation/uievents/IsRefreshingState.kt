package me.proton.pass.presentation.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsRefreshingState {
    object Refreshing : IsRefreshingState
    object NotRefreshing : IsRefreshingState
}

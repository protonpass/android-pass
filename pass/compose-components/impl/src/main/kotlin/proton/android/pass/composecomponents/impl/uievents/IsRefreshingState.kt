package proton.android.pass.composecomponents.impl.uievents

import androidx.compose.runtime.Stable

@Stable
sealed interface IsRefreshingState {
    object Refreshing : IsRefreshingState
    object NotRefreshing : IsRefreshingState
}

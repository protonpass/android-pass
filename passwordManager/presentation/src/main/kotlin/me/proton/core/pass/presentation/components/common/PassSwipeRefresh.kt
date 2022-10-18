package me.proton.core.pass.presentation.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState

@Composable
fun PassSwipeRefresh(
    modifier: Modifier = Modifier,
    state: SwipeRefreshState,
    onRefresh: () -> Unit,
    swipeEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    SwipeRefresh(
        modifier = modifier,
        state = state,
        onRefresh = onRefresh,
        swipeEnabled = swipeEnabled
    ) {
        content()
    }
}

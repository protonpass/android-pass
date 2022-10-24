package me.proton.android.pass.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.ui.internal.InternalDrawerState

@Composable
fun InternalDrawer(
    modifier: Modifier = Modifier,
    drawerState: InternalDrawerState,
    content: @Composable () -> Unit
) {
    content()
}

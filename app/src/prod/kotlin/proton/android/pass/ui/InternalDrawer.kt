package proton.android.pass.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.ui.internal.InternalDrawerState

@Suppress("UnusedPrivateMember")
@Composable
fun InternalDrawer(
    modifier: Modifier = Modifier,
    drawerState: InternalDrawerState,
    content: @Composable () -> Unit,
    onOpenVault: () -> Unit
) {
    content()
}

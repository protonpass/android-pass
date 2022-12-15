package me.proton.pass.presentation.components.navigation.drawer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import me.proton.android.pass.log.InternalLogSharing

@Composable
fun InternalDrawerItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    NavigationDrawerListItem(
        title = "(alpha) Share logs",
        icon = me.proton.core.presentation.R.drawable.ic_proton_cog_wheel,
        isSelected = false,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = {
            coroutineScope.launch {
                InternalLogSharing.shareLogs("me.proton.android.pass.alpha", context)
            }
        }
    )
}

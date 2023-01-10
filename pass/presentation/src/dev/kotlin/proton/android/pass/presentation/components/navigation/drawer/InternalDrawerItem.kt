package proton.android.pass.presentation.components.navigation.drawer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.presentation.R
import proton.android.pass.presentation.navigation.drawer.NavigationDrawerListItem

@Suppress("UnusedPrivateMember")
@Composable
fun InternalDrawerItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit,
    viewModel: InternalDrawerItemViewModel? = null
) {
    NavigationDrawerListItem(
        title = "(dev) Internal developer options",
        icon = R.drawable.ic_proton_cog_wheel,
        isSelected = false,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick
    )
}

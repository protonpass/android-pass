package proton.android.pass.presentation.components.navigation.drawer

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import me.proton.core.compose.theme.ProtonTheme
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
        isSelected = false,
        closeDrawerAction = closeDrawerAction,
        modifier = modifier,
        onClick = onClick,
        startContent = {
            Icon(
                painter = painterResource(R.drawable.ic_proton_cog_wheel),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }
    )
}

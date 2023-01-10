package proton.android.pass.presentation.components.navigation.drawer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.pass.presentation.components.navigation.drawer.InternalDrawerItemViewModel

@Suppress("UnusedPrivateMember", "OptionalUnit")
@Composable
fun InternalDrawerItem(
    modifier: Modifier = Modifier,
    closeDrawerAction: () -> Unit,
    onClick: () -> Unit,
    viewModel: InternalDrawerItemViewModel? = null
) = Unit

package proton.android.pass.featurehome.impl

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView

@Composable
fun IdleHomeTopBar(
    modifier: Modifier = Modifier,
    homeFilter: HomeItemTypeSelection,
    drawerIcon: @Composable () -> Unit,
    startSearchMode: () -> Unit
) {
    val title = when (homeFilter) {
        HomeItemTypeSelection.AllItems -> R.string.title_all_items
        HomeItemTypeSelection.Logins -> R.string.title_all_logins
        HomeItemTypeSelection.Aliases -> R.string.title_all_aliases
        HomeItemTypeSelection.Notes -> R.string.title_all_notes
    }
    ProtonTopAppBar(
        modifier = modifier,
        title = {
            TopBarTitleView(title = stringResource(id = title))
        },
        navigationIcon = {
            drawerIcon()
        },
        actions = {
            IconButton(onClick = {
                startSearchMode()
            }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

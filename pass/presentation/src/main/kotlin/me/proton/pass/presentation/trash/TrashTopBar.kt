package me.proton.pass.presentation.trash

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.ui.shared.HamburgerIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TrashTopBar(
    onDrawerIconClick: () -> Unit,
    onMoreOptionsClick: () -> Unit
) {
    ProtonTopAppBar(
        title = { TopBarTitleView(title = stringResource(id = R.string.title_trash)) },
        navigationIcon = {
            HamburgerIcon(
                onClick = onDrawerIconClick
            )
        },
        actions = {
            IconButton(onClick = onMoreOptionsClick) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

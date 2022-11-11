package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.android.pass.ui.shared.HamburgerIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider

@ExperimentalMaterialApi
@Composable
fun IdleHomeTopBar(
    modifier: Modifier = Modifier,
    startSearchMode: () -> Unit,
    onDrawerIconClick: () -> Unit,
    onMoreOptionsClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        title = {
            TopBarTitleView(title = stringResource(id = me.proton.pass.presentation.R.string.title_items))
        },
        navigationIcon = {
            HamburgerIcon(onClick = onDrawerIconClick)
        },
        actions = {
            IconButton(onClick = {
                startSearchMode()
            }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(me.proton.pass.presentation.R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
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

@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IdleHomeTopBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    ProtonTheme(isDark = isDarkMode) {
        Surface {
            IdleHomeTopBar(
                startSearchMode = {},
                onDrawerIconClick = {},
                onMoreOptionsClick = {}
            )
        }
    }
}

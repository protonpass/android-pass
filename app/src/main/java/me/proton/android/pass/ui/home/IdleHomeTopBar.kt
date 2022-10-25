package me.proton.android.pass.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme

@ExperimentalMaterialApi
@Composable
fun IdleHomeTopBar(
    modifier: Modifier = Modifier,
    startSearchMode: () -> Unit,
    onDrawerIconClick: () -> Unit,
    onAddItemClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        title = {
            TopBarTitleView(title = stringResource(id = me.proton.pass.presentation.R.string.title_items))
        },
        navigationIcon = {
            Icon(
                Icons.Default.Menu,
                modifier = Modifier.clickable { onDrawerIconClick() },
                contentDescription = null
            )
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
            IconButton(onClick = {
                onAddItemClick()
            }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                    contentDescription = stringResource(me.proton.pass.presentation.R.string.action_create),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IdleHomeTopBarPreview() {
    ProtonTheme {
        IdleHomeTopBar(
            startSearchMode = {},
            onDrawerIconClick = {},
            onAddItemClick = {}
        )
    }
}

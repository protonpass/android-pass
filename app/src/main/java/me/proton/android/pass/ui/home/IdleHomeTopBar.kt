package me.proton.android.pass.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme

@ExperimentalMaterialApi
@Composable
fun IdleHomeTopBar(
    drawerState: DrawerState,
    bottomSheetState: ModalBottomSheetState,
    startSearchMode: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    ProtonTopAppBar(
        title = {
            TopBarTitleView(title = stringResource(id = R.string.title_items))
        },
        navigationIcon = {
            Icon(
                Icons.Default.Menu,
                modifier = Modifier.clickable(onClick = {
                    coroutineScope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                }),
                contentDescription = null
            )
        },
        actions = {
            IconButton(onClick = {
                startSearchMode()
            }) {
                Icon(
                    painterResource(R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    if (bottomSheetState.isVisible) {
                        bottomSheetState.hide()
                    } else {
                        bottomSheetState.show()
                    }
                }
            }) {
                Icon(
                    painterResource(R.drawable.ic_proton_plus),
                    contentDescription = stringResource(R.string.action_create),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Preview
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Preview_IdleHomeTopBar() {
    IdleHomeTopBar(
        drawerState = DrawerState(DrawerValue.Closed),
        bottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden),
        startSearchMode = {}
    )
}

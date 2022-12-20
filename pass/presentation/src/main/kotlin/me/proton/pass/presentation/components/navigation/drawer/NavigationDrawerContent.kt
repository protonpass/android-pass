package me.proton.pass.presentation.components.navigation.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.accountmanager.presentation.compose.rememberAccountPrimaryState
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.components.navigation.CoreNavigation

@Composable
fun NavigationDrawerContent(
    modifier: Modifier = Modifier,
    drawerUiState: DrawerUiState,
    accountPrimaryState: AccountPrimaryState = rememberAccountPrimaryState(false),
    navDrawerNavigation: NavDrawerNavigation,
    coreNavigation: CoreNavigation,
    onSignOutClick: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    val sidebarColors = requireNotNull(ProtonTheme.colors.sidebarColors)
    ProtonTheme(colors = sidebarColors) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = ProtonTheme.colors.backgroundNorm
        ) {
            Column {
                NavigationDrawerHeader(
                    drawerUiState = drawerUiState,
                    sidebarColors = sidebarColors,
                    coreNavigation = coreNavigation,
                    accountPrimaryState = accountPrimaryState
                )
                NavigationDrawerBody(
                    modifier = Modifier
                        .padding(top = ProtonDimens.DefaultSpacing)
                        .weight(1f, fill = false),
                    drawerUiState = drawerUiState,
                    navDrawerNavigation = navDrawerNavigation,
                    onSignOutClick = onSignOutClick,
                    onCloseDrawer = onCloseDrawer
                )
            }
        }
    }
}

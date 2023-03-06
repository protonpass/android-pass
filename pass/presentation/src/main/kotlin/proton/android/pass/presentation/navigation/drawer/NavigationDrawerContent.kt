package proton.android.pass.presentation.navigation.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.accountmanager.presentation.compose.rememberAccountPrimaryState
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.presentation.navigation.CoreNavigation

private const val SHOW_NEW_DRAWER_UI = false

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
    val sidebarColors = requireNotNull(ProtonTheme.colors.sidebarColors).copy(
        backgroundNorm = PassTheme.colors.backgroundNorm
    )
    PassTheme(protonColors = sidebarColors) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = PassTheme.colors.backgroundNorm
        ) {
            Column {
                if (SHOW_NEW_DRAWER_UI) {
                    NavigationDrawerVaultSection(
                        modifier = Modifier
                            .padding(top = ProtonDimens.DefaultSpacing)
                            .weight(1f, fill = true),
                        drawerUiState = drawerUiState,
                        navDrawerNavigation = navDrawerNavigation,
                        onVaultOptionsClick = {}, // To be implemented
                        onCloseDrawer = onCloseDrawer
                    )
                    CircleButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = PassTheme.colors.accentPurpleWeakest,
                        elevation = null,
                        onClick = {} // Create vault
                    ) {
                        Text(
                            text = stringResource(R.string.navdrawer_create_vault),
                            color = PassTheme.colors.accentPurpleOpaque,
                            style = PassTypography.body3Regular
                        )
                    }
                } else {
                    NavigationDrawerHeader(
                        currentUser = drawerUiState.currentUser,
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
}

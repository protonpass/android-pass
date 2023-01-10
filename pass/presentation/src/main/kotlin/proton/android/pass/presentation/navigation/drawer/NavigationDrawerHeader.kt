package proton.android.pass.presentation.navigation.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryItem
import me.proton.core.accountmanager.presentation.compose.AccountPrimaryState
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonDimens
import proton.android.pass.presentation.navigation.CoreNavigation

@Composable
fun NavigationDrawerHeader(
    drawerUiState: DrawerUiState,
    sidebarColors: ProtonColors,
    coreNavigation: CoreNavigation,
    accountPrimaryState: AccountPrimaryState
) {
    if (drawerUiState.currentUser != null) {
        AccountPrimaryItem(
            modifier = Modifier
                .background(sidebarColors.backgroundNorm)
                .padding(all = ProtonDimens.SmallSpacing)
                .fillMaxWidth(),
            onRemove = { coreNavigation.onRemove(it) },
            onSignIn = { coreNavigation.onSignIn(it) },
            onSignOut = { coreNavigation.onSignOut(it) },
            onSwitch = { coreNavigation.onSwitch(it) },
            viewState = accountPrimaryState
        )
    }
}

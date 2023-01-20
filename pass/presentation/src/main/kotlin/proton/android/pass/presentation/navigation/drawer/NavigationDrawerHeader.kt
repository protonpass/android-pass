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
import me.proton.core.user.domain.entity.User
import proton.android.pass.presentation.navigation.CoreNavigation

@Composable
fun NavigationDrawerHeader(
    currentUser: User?,
    sidebarColors: ProtonColors,
    coreNavigation: CoreNavigation,
    accountPrimaryState: AccountPrimaryState
) {
    currentUser ?: return
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

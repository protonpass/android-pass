package proton.android.pass.featureaccount.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

object Account : NavItem(baseRoute = "account/view")
object SignOutDialog : NavItem(baseRoute = "account/signout/dialog")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.accountGraph(
    onNavigate: (AccountNavigation) -> Unit
) {
    composable(Account) {
        AccountScreen(
            onNavigate = onNavigate
        )
    }

    dialog(SignOutDialog) {
        ConfirmSignOutDialog(
            onNavigate = onNavigate
        )
    }
}


sealed interface AccountNavigation {
    object Subscription : AccountNavigation
    object Upgrade : AccountNavigation
    object SignOut : AccountNavigation
    object ConfirmSignOut : AccountNavigation
    object DismissDialog : AccountNavigation
    object Back : AccountNavigation
}

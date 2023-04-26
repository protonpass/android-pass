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
    onSignOutClick: () -> Unit,
    onDismissClick: () -> Unit,
    onConfirmSignOutClick: () -> Unit,
    onUpClick: () -> Unit,
    onCurrentSubscriptionClick: () -> Unit
) {
    composable(Account) {
        AccountScreen(
            onSignOutClick = onSignOutClick,
            onUpClick = onUpClick,
            onCurrentSubscriptionClick = onCurrentSubscriptionClick
        )
    }

    dialog(SignOutDialog) {
        ConfirmSignOutDialog(
            show = true,
            onDismiss = onDismissClick,
            onConfirm = onConfirmSignOutClick
        )
    }
}

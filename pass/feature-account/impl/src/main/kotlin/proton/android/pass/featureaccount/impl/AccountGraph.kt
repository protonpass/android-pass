package proton.android.pass.featureaccount.impl

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog

object Account : NavItem(baseRoute = "account/view")
object SignOutDialog : NavItem(baseRoute = "account/signout/dialog")

@OptIn(ExperimentalAnimationApi::class)
@Suppress("LongParameterList")
fun NavGraphBuilder.accountGraph(
    onSubscriptionClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDismissClick: () -> Unit,
    onConfirmSignOutClick: () -> Unit,
    onUpClick: () -> Unit
) {
    composable(Account) {
        AccountScreen(
            onSubscriptionClick = onSubscriptionClick,
            onUpgradeClick = onUpgradeClick,
            onSignOutClick = onSignOutClick,
            onUpClick = onUpClick
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

package proton.android.pass.featuresettings.impl

import androidx.navigation.NavGraphBuilder
import proton.android.pass.composecomponents.impl.dialogs.ConfirmSignOutDialog
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.dialog

object SignOutDialog : NavItem(baseRoute = "account/signout/dialog")

fun NavGraphBuilder.signOutDialogGraph(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    dialog(SignOutDialog) {
        ConfirmSignOutDialog(
            show = true,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

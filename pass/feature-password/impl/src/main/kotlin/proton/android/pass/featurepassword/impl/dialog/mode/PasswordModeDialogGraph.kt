package proton.android.pass.featurepassword.impl.dialog.mode

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.dialog

object PasswordModeDialog : NavItem(baseRoute = "password/create/passwordmode")

fun NavGraphBuilder.passwordModeDialog(
    onNavigate: (GeneratePasswordNavigation) -> Unit
) {
    dialog(PasswordModeDialog) {
        PasswordModeDialog(
            onNavigate = onNavigate
        )
    }
}

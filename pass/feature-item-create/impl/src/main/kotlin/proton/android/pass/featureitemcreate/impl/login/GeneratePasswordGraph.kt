package proton.android.pass.featureitemcreate.impl.login

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.password.GeneratePasswordBottomSheet
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object GenerateLoginPasswordBottomsheet : NavItem(baseRoute = "password/create/login/bottomsheet")

fun NavGraphBuilder.generatePasswordGraph(
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    bottomSheet(GenerateLoginPasswordBottomsheet) {
        GeneratePasswordBottomSheet(
            onConfirm = { dismissBottomSheet {} },
            onDismiss = { dismissBottomSheet {} }
        )
    }
}

package proton.android.pass.featureitemcreate.impl.bottomsheets.generatepassword

import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object GeneratePasswordBottomsheet : NavItem(baseRoute = "password/create/bottomsheet")

fun NavGraphBuilder.generatePasswordBottomsheetGraph(
    onDismiss: () -> Unit
) {
    bottomSheet(GeneratePasswordBottomsheet) {
        GeneratePasswordBottomSheet(onDismiss = onDismiss)
    }
}

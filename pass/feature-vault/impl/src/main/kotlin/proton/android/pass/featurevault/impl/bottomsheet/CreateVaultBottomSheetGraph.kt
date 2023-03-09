package proton.android.pass.featurevault.impl.bottomsheet

import androidx.navigation.NavGraphBuilder
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object CreateVaultBottomSheet : NavItem(baseRoute = "vault/create/bottomsheet")
object EditVaultBottomSheet : NavItem(baseRoute = "vault/edit/bottomsheet")

fun NavGraphBuilder.bottomSheetCreateVaultGraph(
    onClose: () -> Unit
) {
    bottomSheet(CreateVaultBottomSheet) {
        CreateVaultBottomSheet(
            onClose = onClose
        )
    }
}
fun NavGraphBuilder.bottomSheetEditVaultGraph(
    onClose: () -> Unit
) {
    bottomSheet(CreateVaultBottomSheet) {
        CreateVaultBottomSheet(
            onClose = onClose
        )
    }
}


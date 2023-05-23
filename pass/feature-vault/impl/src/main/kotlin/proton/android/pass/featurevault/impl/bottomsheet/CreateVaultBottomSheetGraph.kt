package proton.android.pass.featurevault.impl.bottomsheet

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ShareId

object CreateVaultBottomSheet : NavItem(baseRoute = "vault/create/bottomsheet")
object EditVaultBottomSheet : NavItem(
    baseRoute = "vault/edit/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createNavRoute(shareId: ShareId) = buildString {
        append("$baseRoute/${shareId.id}")
    }
}

internal fun NavGraphBuilder.bottomSheetCreateVaultGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    bottomSheet(CreateVaultBottomSheet) {
        CreateVaultBottomSheet(
            onNavigate = onNavigate
        )
    }
}

internal fun NavGraphBuilder.bottomSheetEditVaultGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    bottomSheet(EditVaultBottomSheet) {
        EditVaultBottomSheet(
            onNavigate = onNavigate
        )
    }
}


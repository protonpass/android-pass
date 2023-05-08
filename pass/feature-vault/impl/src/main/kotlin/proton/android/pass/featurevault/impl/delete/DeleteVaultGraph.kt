package proton.android.pass.featurevault.impl.delete

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.dialog
import proton.pass.domain.ShareId

object DeleteVaultDialog : NavItem(
    baseRoute = "vault/delete/dialog",
    navArgIds = listOf(CommonNavArgId.ShareId)
) {
    fun createNavRoute(shareId: ShareId): String = "$baseRoute/${shareId.id}"
}

fun NavGraphBuilder.deleteVaultDialogGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    dialog(DeleteVaultDialog) {
        DeleteVaultDialog(
            onNavigate = onNavigate
        )
    }
}


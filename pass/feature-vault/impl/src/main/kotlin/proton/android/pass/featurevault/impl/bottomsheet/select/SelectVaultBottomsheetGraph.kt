package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ShareId

object SelectedVaultArg : NavArgId {
    override val key = "selectedVault"
    override val navType = NavType.StringType
}

object SelectVaultBottomsheet : NavItem(
    baseRoute = "vault/select/bottomsheet",
    navArgIds = listOf(SelectedVaultArg)
) {
    fun createNavRoute(selectedVault: ShareId) = "$baseRoute/${selectedVault.id}"
}

fun NavGraphBuilder.selectVaultBottomsheetGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    bottomSheet(SelectVaultBottomsheet) {
        SelectVaultBottomsheet(
            onNavigate = onNavigate
        )
    }
}

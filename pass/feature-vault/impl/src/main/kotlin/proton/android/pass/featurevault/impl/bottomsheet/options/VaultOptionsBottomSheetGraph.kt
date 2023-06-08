package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.pass.domain.ShareId

object VaultOptionsBottomSheet : NavItem(
    baseRoute = "vault/options/bottomsheet",
    navArgIds = listOf(CommonNavArgId.ShareId),
    isBottomsheet = true
) {
    fun createNavRoute(shareId: ShareId) = buildString {
        append("$baseRoute/${shareId.id}")
    }
}

internal fun NavGraphBuilder.bottomSheetVaultOptionsGraph(
    onNavigate: (VaultNavigation) -> Unit
) {
    bottomSheet(VaultOptionsBottomSheet) {
        VaultOptionsBottomSheet(onNavigate = onNavigate)
    }
}

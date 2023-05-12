package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object SelectedVaultOptionalArg : OptionalNavArgId {
    override val key = "selectedVault"
    override val navType = NavType.StringType
}

object SelectVaultBottomsheet : NavItem(
    baseRoute = "vault/select/bottomsheet",
    optionalArgIds = listOf(SelectedVaultOptionalArg)
) {
    fun createNavRoute(selectedVault: Option<ShareId>) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (selectedVault is Some) {
            map[SelectedVaultOptionalArg.key] = selectedVault.value
        }
        val path = map.toPath()
        append(path)
    }
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

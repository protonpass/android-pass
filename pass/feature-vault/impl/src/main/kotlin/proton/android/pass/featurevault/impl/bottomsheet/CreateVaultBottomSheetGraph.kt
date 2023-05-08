package proton.android.pass.featurevault.impl.bottomsheet

import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object CreateVaultBottomSheet : NavItem(baseRoute = "vault/create/bottomsheet")
object EditVaultBottomSheet : NavItem(
    baseRoute = "vault/edit/bottomsheet",
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
    )
) {
    fun createNavRoute(shareId: Option<ShareId>) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
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


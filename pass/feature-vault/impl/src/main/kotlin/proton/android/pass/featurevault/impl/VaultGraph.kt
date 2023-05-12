package proton.android.pass.featurevault.impl

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetCreateVaultGraph
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetEditVaultGraph
import proton.android.pass.featurevault.impl.bottomsheet.select.selectVaultBottomsheetGraph
import proton.android.pass.featurevault.impl.delete.deleteVaultDialogGraph
import proton.pass.domain.ShareId

sealed interface VaultNavigation {
    object Upgrade : VaultNavigation
    object Close : VaultNavigation
    data class VaultSelected(
        val shareId: ShareId
    ) : VaultNavigation
}

fun NavGraphBuilder.vaultGraph(
    onNavigate: (VaultNavigation) -> Unit,
) {
    bottomSheetCreateVaultGraph(onNavigate)
    bottomSheetEditVaultGraph(onNavigate)
    deleteVaultDialogGraph(onNavigate)
    selectVaultBottomsheetGraph(onNavigate)
}

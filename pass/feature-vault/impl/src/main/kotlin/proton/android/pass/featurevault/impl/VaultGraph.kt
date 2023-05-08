package proton.android.pass.featurevault.impl

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetCreateVaultGraph
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetEditVaultGraph
import proton.android.pass.featurevault.impl.delete.deleteVaultDialogGraph

sealed interface VaultNavigation {
    object Upgrade : VaultNavigation
    object Close : VaultNavigation
}

fun NavGraphBuilder.vaultGraph(
    onNavigate: (VaultNavigation) -> Unit,
) {
    bottomSheetCreateVaultGraph(onNavigate)
    bottomSheetEditVaultGraph(onNavigate)
    deleteVaultDialogGraph(onNavigate)
}

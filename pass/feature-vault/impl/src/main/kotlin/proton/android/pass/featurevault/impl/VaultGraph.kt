package proton.android.pass.featurevault.impl

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetCreateVaultGraph
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetEditVaultGraph
import proton.android.pass.featurevault.impl.delete.deleteVaultDialogGraph

fun NavGraphBuilder.vaultGraph(
    dismissBottomSheet: () -> Unit,
    onClose: () -> Unit
) {
    bottomSheetCreateVaultGraph(dismissBottomSheet)
    bottomSheetEditVaultGraph(dismissBottomSheet)
    deleteVaultDialogGraph(onClose)
}

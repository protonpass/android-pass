package proton.android.pass.featuremigrate.impl.selectvault

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.pass.domain.ShareId

@Composable
fun MigrateSelectVaultContents(
    modifier: Modifier = Modifier,
    vaults: ImmutableList<VaultEnabledPair>,
    onVaultSelected: (ShareId) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier,
        items = vaults.map { vault ->
            BottomSheetVaultRow(
                vault = vault.vault,
                isSelected = false,
                enabled = vault.isEnabled,
                onVaultClick = { onVaultSelected(vault.vault.vault.shareId) }
            )
        }
            .withDividers()
            .toImmutableList(),
    )
}

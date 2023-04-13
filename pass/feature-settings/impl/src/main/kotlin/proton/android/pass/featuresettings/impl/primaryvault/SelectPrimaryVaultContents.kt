package proton.android.pass.featuresettings.impl.primaryvault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

@Composable
fun SelectPrimaryVaultContents(
    modifier: Modifier = Modifier,
    vaults: ImmutableList<VaultWithItemCount>,
    loading: Boolean,
    onVaultSelected: (VaultWithItemCount) -> Unit
) {
    var vaultBeingUpdated: ShareId? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(loading) {
        if (!loading) {
            vaultBeingUpdated = null
        }
    }

    BottomSheetItemList(
        modifier = modifier,
        items = vaults.map { vault ->
            BottomSheetVaultRow(
                vault = vault,
                isSelected = false,
                enabled = !loading,
                isLoading = vault.vault.shareId == vaultBeingUpdated,
                onVaultClick = {
                    vaultBeingUpdated = vault.vault.shareId
                    onVaultSelected(vault)
                }
            )
        }
            .withDividers()
            .toImmutableList(),
    )

}

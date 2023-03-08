package proton.android.pass.featurehome.impl.vault

import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.featurehome.impl.HomeVaultSelection

data class VaultDrawerUiState(
    val itemCountSummary: ItemCountSummary,
    val vaultSelection: HomeVaultSelection,
    val shares: ImmutableList<ShareUiModelWithItemCount>,
    val totalTrashedItems: Long
)

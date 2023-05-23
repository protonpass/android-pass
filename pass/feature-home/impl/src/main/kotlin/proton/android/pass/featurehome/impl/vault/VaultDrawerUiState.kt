package proton.android.pass.featurehome.impl.vault

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.featurehome.impl.HomeVaultSelection

@Immutable
data class VaultDrawerUiState(
    val vaultSelection: HomeVaultSelection,
    val shares: ImmutableList<ShareUiModelWithItemCount>,
    val totalTrashedItems: Long
)

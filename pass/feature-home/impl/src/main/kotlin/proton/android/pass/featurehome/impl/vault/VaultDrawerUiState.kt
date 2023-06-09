package proton.android.pass.featurehome.impl.vault

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption

@Immutable
data class VaultDrawerUiState(
    val vaultSelection: VaultSelectionOption,
    val shares: ImmutableList<ShareUiModelWithItemCount>,
    val totalTrashedItems: Long
)

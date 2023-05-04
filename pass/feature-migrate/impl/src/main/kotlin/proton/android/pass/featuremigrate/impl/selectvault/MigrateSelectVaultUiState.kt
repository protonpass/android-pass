package proton.android.pass.featuremigrate.impl.selectvault

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

sealed interface SelectVaultEvent {
    data class SelectedVault(
        val sourceShareId: ShareId,
        val itemId: ItemId,
        val destinationShareId: ShareId
    ) : SelectVaultEvent
    object Close : SelectVaultEvent
}

data class VaultEnabledPair(
    val vault: VaultWithItemCount,
    val isEnabled: Boolean
)

@Stable
data class MigrateSelectVaultUiState(
    val vaultList: ImmutableList<VaultEnabledPair>,
    val event: Option<SelectVaultEvent>
) {
    companion object {
        val Initial = MigrateSelectVaultUiState(
            vaultList = persistentListOf(),
            event = None
        )
    }
}

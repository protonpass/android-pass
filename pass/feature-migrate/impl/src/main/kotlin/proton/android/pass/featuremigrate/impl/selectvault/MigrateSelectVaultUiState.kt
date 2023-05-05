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
    data class VaultSelectedForMigrateItem(
        val sourceShareId: ShareId,
        val itemId: ItemId,
        val destinationShareId: ShareId
    ) : SelectVaultEvent

    data class VaultSelectedForMigrateAll(
        val sourceShareId: ShareId,
        val destinationShareId: ShareId
    ) : SelectVaultEvent

    object Close : SelectVaultEvent
}

enum class MigrateMode {
    MigrateItem,
    MigrateAll
}

data class VaultEnabledPair(
    val vault: VaultWithItemCount,
    val isEnabled: Boolean
)

@Stable
data class MigrateSelectVaultUiState(
    val vaultList: ImmutableList<VaultEnabledPair>,
    val event: Option<SelectVaultEvent>,
    val mode: MigrateMode
) {
    companion object {
        fun Initial(mode: MigrateMode) = MigrateSelectVaultUiState(
            vaultList = persistentListOf(),
            event = None,
            mode = mode
        )
    }
}

package proton.android.pass.featuremigrate.impl.selectvault

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
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

sealed class MigrateSelectVaultUiState {
    @Stable
    object Uninitialised : MigrateSelectVaultUiState()

    @Stable
    object Loading : MigrateSelectVaultUiState()

    @Stable
    object Error : MigrateSelectVaultUiState()

    @Stable
    data class Success(
        val vaultList: ImmutableList<VaultEnabledPair>,
        val event: Option<SelectVaultEvent>,
        val mode: MigrateMode
    ) : MigrateSelectVaultUiState()
}

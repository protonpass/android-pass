package proton.android.pass.featuremigrate.impl.confirmvault

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount

sealed interface ConfirmMigrateEvent {
    object Close : ConfirmMigrateEvent
    data class Migrated(val shareId: ShareId, val itemId: ItemId) : ConfirmMigrateEvent
}

@Stable
data class MigrateConfirmVaultUiState(
    val isLoading: IsLoadingState,
    val event: Option<ConfirmMigrateEvent>,
    val vault: Option<VaultWithItemCount>
) {
    companion object {
        val Initial = MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.Loading,
            event = None,
            vault = None
        )
    }
}

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
    data class ItemMigrated(val shareId: ShareId, val itemId: ItemId) : ConfirmMigrateEvent
    object AllItemsMigrated : ConfirmMigrateEvent
}

enum class MigrateMode {
    MigrateItem,
    MigrateAll
}

@Stable
data class MigrateConfirmVaultUiState(
    val isLoading: IsLoadingState,
    val event: Option<ConfirmMigrateEvent>,
    val vault: Option<VaultWithItemCount>,
    val mode: MigrateMode
) {
    companion object {
        fun Initial(mode: MigrateMode) = MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.NotLoading,
            event = None,
            vault = None,
            mode = mode
        )
    }
}

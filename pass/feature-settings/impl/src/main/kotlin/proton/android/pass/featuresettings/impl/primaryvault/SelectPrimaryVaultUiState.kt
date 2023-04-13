package proton.android.pass.featuresettings.impl.primaryvault

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.VaultWithItemCount

sealed interface SelectPrimaryVaultEvent {
    object Unknown : SelectPrimaryVaultEvent
    object Selected : SelectPrimaryVaultEvent
}

@Stable
data class SelectPrimaryVaultUiState(
    val vaults: ImmutableList<VaultWithItemCount>,
    val event: SelectPrimaryVaultEvent,
    val loading: IsLoadingState
) {
    companion object {
        val Initial = SelectPrimaryVaultUiState(
            vaults = persistentListOf(),
            event = SelectPrimaryVaultEvent.Unknown,
            loading = IsLoadingState.NotLoading
        )
    }
}

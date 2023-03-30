package proton.android.pass.featurevault.impl.delete

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface DeleteVaultEvent {
    object Unknown : DeleteVaultEvent
    object Deleted : DeleteVaultEvent
}

@Stable
data class DeleteVaultUiState(
    val vaultName: String,
    val vaultText: String,
    val event: DeleteVaultEvent,
    val isButtonEnabled: IsButtonEnabled,
    val isLoadingState: IsLoadingState
) {
    companion object {
        val Initial = DeleteVaultUiState(
            vaultName = "",
            vaultText = "",
            event = DeleteVaultEvent.Unknown,
            isButtonEnabled = IsButtonEnabled.Disabled,
            isLoadingState = IsLoadingState.NotLoading
        )
    }
}

package proton.android.pass.featurevault.impl.delete

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

class DeleteVaultDialogPreviewProvider : PreviewParameterProvider<DeleteVaultUiState> {
    override val values: Sequence<DeleteVaultUiState>
        get() = sequence {
            for (button in listOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)) {
                for (loading in listOf(IsLoadingState.Loading, IsLoadingState.NotLoading)) {
                    for (text in listOf("", "vaultname")) {
                        yield(
                            DeleteVaultUiState(
                                vaultName = text,
                                vaultText = text,
                                event = DeleteVaultEvent.Unknown,
                                isButtonEnabled = button,
                                isLoadingState = loading
                            )
                        )
                    }
                }
            }
        }
}

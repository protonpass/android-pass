package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

class CreateVaultProvider : PreviewParameterProvider<BaseVaultUiState> {
    override val values: Sequence<BaseVaultUiState>
        get() = sequenceOf(
            stateWith(name = "", isError = true),
            stateWith(name = ""),
            stateWith(name = "some vault"),
            stateWith(name = "some vault", isLoading = IsLoadingState.Loading),
        )

    private fun stateWith(
        name: String,
        isError: Boolean = false,
        isLoading: IsLoadingState = IsLoadingState.NotLoading
    ) = BaseVaultUiState(
        name = name,
        color = ShareColor.Color1,
        icon = ShareIcon.Icon1,
        isLoading = isLoading,
        isTitleRequiredError = isError,
        isVaultCreatedEvent = IsVaultCreatedEvent.Unknown,
        isCreateButtonEnabled = IsButtonEnabled.Enabled
    )
}

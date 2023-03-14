package proton.android.pass.featureitemcreate.impl.totp

import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

data class CreateManualTotpUiState(
    val totpSpec: TotpSpecUi,
    val isTotpUriCreatedState: IsTotpUriCreatedState,
    val isLoadingState: IsLoadingState,
    val validationErrors: Set<TotpSpecValidationErrors>
) {
    companion object {
        val Initial = CreateManualTotpUiState(
            totpSpec = TotpSpecUi("", "", ""),
            isTotpUriCreatedState = IsTotpUriCreatedState.Unknown,
            isLoadingState = IsLoadingState.NotLoading,
            validationErrors = emptySet()
        )
    }
}

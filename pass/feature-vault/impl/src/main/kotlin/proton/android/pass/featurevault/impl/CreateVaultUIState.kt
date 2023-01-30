package proton.android.pass.featurevault.impl

import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

data class CreateVaultUIState(
    val draftVault: DraftVaultUiState,
    val isLoadingState: IsLoadingState,
    val vaultSavedState: VaultSavedState,
    val validationErrors: Set<DraftVaultValidationErrors>
) {
    companion object {
        val Initial = CreateVaultUIState(
            draftVault = DraftVaultUiState("", ""),
            isLoadingState = IsLoadingState.NotLoading,
            vaultSavedState = VaultSavedState.Unknown,
            validationErrors = emptySet()
        )
    }
}

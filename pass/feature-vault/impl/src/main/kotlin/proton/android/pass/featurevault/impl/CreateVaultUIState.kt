package proton.android.pass.featurevault.impl

import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

data class CreateVaultUIState(
    val draftVault: CreateVaultViewModel.DraftVault,
    val isLoadingState: IsLoadingState,
    val vaultSavedState: VaultSavedState
) {
    companion object {
        val Initial = CreateVaultUIState(
            draftVault = CreateVaultViewModel.DraftVault("", ""),
            isLoadingState = IsLoadingState.NotLoading,
            vaultSavedState = VaultSavedState.Unknown
        )
    }
}

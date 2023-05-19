package proton.android.pass.featureitemcreate.impl.login

import proton.pass.domain.VaultWithItemCount

sealed class ShareUiState {
    object NotInitialised : ShareUiState()
    object Loading : ShareUiState()
    data class Error(val shareError: ShareError) : ShareUiState()
    data class Success(
        val vaultList: List<VaultWithItemCount>,
        val currentVault: VaultWithItemCount
    ) : ShareUiState()
}

enum class ShareError {
    EmptyShareList,
    UpgradeInfoNotAvailable,
    SharesNotAvailable,
    NoPrimaryVault
}

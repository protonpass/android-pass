package proton.android.pass.featurevault.impl.bottomsheet.options

import proton.pass.domain.ShareId

sealed class VaultOptionsUiState {
    object Uninitialised : VaultOptionsUiState()
    object Loading : VaultOptionsUiState()
    object Error : VaultOptionsUiState()
    data class Success(
        val shareId: ShareId,
        val showEdit: Boolean,
        val showMigrate: Boolean,
        val showDelete: Boolean,
    ) : VaultOptionsUiState()
}

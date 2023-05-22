package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import proton.pass.domain.VaultWithItemCount

sealed interface SelectVaultUiState {
    object Uninitialised : SelectVaultUiState
    object Loading : SelectVaultUiState
    object Error : SelectVaultUiState

    @Immutable
    data class Success(
        val vaults: ImmutableList<VaultWithItemCount>,
        val selected: VaultWithItemCount
    ) : SelectVaultUiState
}

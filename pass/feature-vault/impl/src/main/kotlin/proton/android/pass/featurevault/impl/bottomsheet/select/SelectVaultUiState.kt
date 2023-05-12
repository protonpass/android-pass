package proton.android.pass.featurevault.impl.bottomsheet.select

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.pass.domain.VaultWithItemCount

sealed interface SelectVaultUiEvent {
    object Unknown : SelectVaultUiEvent
    object Close : SelectVaultUiEvent
}

data class SelectVaultUiState(
    val vaults: List<VaultWithItemCount>,
    val selected: Option<VaultWithItemCount>,
    val event: SelectVaultUiEvent
) {
    companion object {
        val Initial = SelectVaultUiState(
            vaults = emptyList(),
            selected = None,
            event = SelectVaultUiEvent.Unknown
        )
    }
}

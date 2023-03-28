package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.VaultWithItemCount

@Immutable
data class CreateUpdateAliasUiState(
    val vaultList: List<VaultWithItemCount>,
    val selectedVault: VaultWithItemCount?,
    val aliasItem: AliasItem,
    val isDraft: Boolean,
    val errorList: Set<AliasItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isAliasSavedState: AliasSavedState,
    val isAliasDraftSavedState: AliasDraftSavedState,
    val isApplyButtonEnabled: IsButtonEnabled,
    val showVaultSelector: Boolean,
    val closeScreenEvent: CloseScreenEvent
) {
    companion object {
        val Initial = CreateUpdateAliasUiState(
            vaultList = emptyList(),
            selectedVault = null,
            aliasItem = AliasItem.Empty,
            isDraft = false,
            errorList = emptySet(),
            isLoadingState = IsLoadingState.Loading,
            isAliasSavedState = AliasSavedState.Unknown,
            isAliasDraftSavedState = AliasDraftSavedState.Unknown,
            isApplyButtonEnabled = IsButtonEnabled.Disabled,
            showVaultSelector = false,
            closeScreenEvent = CloseScreenEvent.NotClose
        )
    }
}

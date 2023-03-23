package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.pass.domain.VaultWithItemCount

@Immutable
data class CreateUpdateNoteUiState(
    val vaultList: List<VaultWithItemCount>,
    val selectedVault: VaultWithItemCount?,
    val noteItem: NoteItem,
    val errorList: Set<NoteItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val showVaultSelector: Boolean
) {
    companion object {
        val Initial = CreateUpdateNoteUiState(
            vaultList = emptyList(),
            selectedVault = null,
            isLoadingState = IsLoadingState.NotLoading,
            noteItem = NoteItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            showVaultSelector = false
        )
    }
}

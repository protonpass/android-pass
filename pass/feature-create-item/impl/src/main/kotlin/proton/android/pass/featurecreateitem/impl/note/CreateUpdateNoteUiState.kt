package proton.android.pass.featurecreateitem.impl.note

import androidx.compose.runtime.Immutable
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState

@Immutable
data class CreateUpdateNoteUiState(
    val shareList: List<ShareUiModel>,
    val selectedShareId: ShareUiModel?,
    val noteItem: NoteItem,
    val errorList: Set<NoteItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val isSentToTrash: IsSentToTrashState
) {
    companion object {
        val Initial = CreateUpdateNoteUiState(
            shareList = emptyList(),
            selectedShareId = null,
            isLoadingState = IsLoadingState.NotLoading,
            noteItem = NoteItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            isSentToTrash = IsSentToTrashState.NotSent
        )
    }
}

package proton.android.pass.featurecreateitem.impl.note

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.pass.domain.ShareId

@Immutable
data class CreateUpdateNoteUiState(
    val shareId: Option<ShareId>,
    val noteItem: NoteItem,
    val errorList: Set<NoteItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val isSentToTrash: IsSentToTrashState
) {
    companion object {
        val Initial = CreateUpdateNoteUiState(
            shareId = None,
            isLoadingState = IsLoadingState.NotLoading,
            noteItem = NoteItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            isSentToTrash = IsSentToTrashState.NotSent
        )
    }
}

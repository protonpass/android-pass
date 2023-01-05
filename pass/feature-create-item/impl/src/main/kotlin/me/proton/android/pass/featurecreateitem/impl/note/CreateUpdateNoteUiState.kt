package me.proton.android.pass.featurecreateitem.impl.note

import androidx.compose.runtime.Immutable
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import me.proton.android.pass.featurecreateitem.impl.ItemSavedState
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId

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

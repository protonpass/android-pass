package me.proton.android.pass.ui.create.note

import androidx.compose.runtime.Immutable
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.android.pass.ui.shared.uievents.ItemSavedState

@Immutable
data class CreateUpdateNoteUiState(
    val noteItem: NoteItem,
    val errorList: Set<NoteItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState
) {
    companion object {
        val Initial = CreateUpdateNoteUiState(
            isLoadingState = IsLoadingState.NotLoading,
            noteItem = NoteItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown
        )
    }
}

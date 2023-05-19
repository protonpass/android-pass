package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.login.ShareUiState
import proton.pass.domain.ShareId

@Immutable
data class BaseNoteUiState(
    val noteItem: NoteItem,
    val errorList: Set<NoteItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val hasUserEditedContent: Boolean
) {
    companion object {
        val Initial = BaseNoteUiState(
            isLoadingState = IsLoadingState.NotLoading,
            noteItem = NoteItem.Empty,
            errorList = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            hasUserEditedContent = false
        )
    }
}

@Immutable
data class CreateNoteUiState(
    val shareUiState: ShareUiState,
    val baseNoteUiState: BaseNoteUiState
) {
    companion object {
        val Initial = CreateNoteUiState(
            shareUiState = ShareUiState.NotInitialised,
            baseNoteUiState = BaseNoteUiState.Initial
        )
    }
}

@Immutable
data class UpdateNoteUiState(
    val selectedShareId: ShareId?,
    val baseNoteUiState: BaseNoteUiState
) {
    companion object {
        val Initial = UpdateNoteUiState(
            selectedShareId = null,
            baseNoteUiState = BaseNoteUiState.Initial
        )
    }
}

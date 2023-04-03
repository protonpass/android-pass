package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.ItemUiModel

sealed interface NoteDetailUiState {

    @Stable
    object NotInitialised : NoteDetailUiState

    @Stable
    object Error : NoteDetailUiState

    @Stable
    data class Success(
        val itemUiModel: ItemUiModel,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
        val isPermanentlyDeleted: Boolean,
        val isRestoredFromTrash: Boolean,
    ) : NoteDetailUiState
}

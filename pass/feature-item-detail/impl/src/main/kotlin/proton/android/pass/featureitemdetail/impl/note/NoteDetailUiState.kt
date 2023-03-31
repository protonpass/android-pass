package proton.android.pass.featureitemdetail.impl.note

import androidx.compose.runtime.Stable
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

sealed interface NoteDetailUiState {

    @Stable
    object NotInitialised : NoteDetailUiState

    @Stable
    object Error : NoteDetailUiState

    @Stable
    data class Success(
        val shareId: ShareId,
        val itemId: ItemId,
        val title: String,
        val note: String,
        val itemType: ItemType,
        val state: Int,
        val isLoading: Boolean,
        val isItemSentToTrash: Boolean,
    ) : NoteDetailUiState
}

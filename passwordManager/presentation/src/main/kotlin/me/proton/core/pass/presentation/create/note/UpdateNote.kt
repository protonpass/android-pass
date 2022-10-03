package me.proton.core.pass.presentation.create.note

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.R

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateNote(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
    itemId: ItemId
) {
    val viewModel: UpdateNoteViewModel = hiltViewModel()
    viewModel.setItem(shareId, itemId)
    val noteUiState by viewModel.noteUiState.collectAsState()
    NoteContent(
        uiState = noteUiState,
        topBarTitle = R.string.title_edit_note,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}

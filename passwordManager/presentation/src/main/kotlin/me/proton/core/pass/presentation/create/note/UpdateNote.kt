package me.proton.core.pass.presentation.create.note

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.create.note.NoteSnackbarMessage.NoteUpdated

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateNote(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    viewModel: UpdateNoteViewModel = hiltViewModel()
) {
    val noteUiState by viewModel.noteUiState.collectAsStateWithLifecycle()

    NoteContent(
        modifier = modifier,
        uiState = noteUiState,
        topBarTitle = R.string.title_edit_note,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = { shareId, itemId ->
            viewModel.onEmitSnackbarMessage(NoteUpdated)
            onSuccess(shareId, itemId)
        },
        onSubmit = { shareId -> viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) }
    )
}

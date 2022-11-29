package me.proton.pass.presentation.create.note

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.presentation.R
import me.proton.pass.presentation.create.note.NoteSnackbarMessage.NoteCreated

@OptIn(ExperimentalLifecycleComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateNote(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val noteUiState by viewModel.noteUiState.collectAsStateWithLifecycle()

    NoteContent(
        modifier = modifier,
        uiState = noteUiState,
        topBarTitle = R.string.title_create_note,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = { _, _ ->
            viewModel.onEmitSnackbarMessage(NoteCreated)
            onSuccess()
        },
        onSubmit = { shareId -> viewModel.createNote(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) }
    )
}

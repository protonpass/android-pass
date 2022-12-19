package me.proton.pass.presentation.create.note

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.dialogs.ConfirmMoveItemToTrashDialog
import me.proton.pass.presentation.create.note.NoteSnackbarMessage.NoteUpdated
import me.proton.pass.presentation.uievents.IsSentToTrashState

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UpdateNote(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    onSentToTrash: () -> Unit,
    viewModel: UpdateNoteViewModel = hiltViewModel()
) {
    val noteUiState by viewModel.noteUiState.collectAsStateWithLifecycle()

    LaunchedEffect(noteUiState.isSentToTrash) {
        if (noteUiState.isSentToTrash == IsSentToTrashState.Sent) {
            onSentToTrash()
        }
    }

    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        NoteContent(
            uiState = noteUiState,
            topBarTitle = stringResource(R.string.title_edit_note),
            topBarActionName = stringResource(R.string.action_save),
            canDelete = true,
            onUpClick = onUpClick,
            onSuccess = { shareId, itemId ->
                viewModel.onEmitSnackbarMessage(NoteUpdated)
                onSuccess(shareId, itemId)
            },
            onSubmit = { shareId -> viewModel.updateItem(shareId) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) },
            onDelete = { setShowDeleteDialog(true) }
        )

        ConfirmMoveItemToTrashDialog(
            show = showDeleteDialog,
            itemName = noteUiState.noteItem.title,
            onConfirm = {
                viewModel.onDelete()
                setShowDeleteDialog(false)
            },
            onDismiss = { setShowDeleteDialog(false) },
            onCancel = { setShowDeleteDialog(false) }
        )
    }

}

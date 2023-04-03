package proton.android.pass.featureitemcreate.impl.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.featureitemcreate.impl.R

@OptIn(ExperimentalLifecycleComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateNoteScreen(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val noteUiState by viewModel.noteUiState.collectAsStateWithLifecycle()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (noteUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            onUpClick()
        }
    }
    BackHandler {
        onExit()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NoteContent(
            uiState = noteUiState,
            topBarActionName = stringResource(R.string.title_create_note),
            showVaultSelector = noteUiState.showVaultSelector,
            onUpClick = onExit,
            onSuccess = { _, _ -> onSuccess() },
            onSubmit = { shareId -> viewModel.createNote(shareId) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onVaultSelect = { viewModel.changeVault(it) }
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                onUpClick()
            }
        )
    }
}

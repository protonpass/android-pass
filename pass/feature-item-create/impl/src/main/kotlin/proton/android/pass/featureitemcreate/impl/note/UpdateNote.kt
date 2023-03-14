package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class)
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
        topBarActionName = stringResource(R.string.action_save),
        showVaultSelector = false,
        onUpClick = onUpClick,
        onSuccess = { shareId, itemId -> onSuccess(shareId, itemId) },
        onSubmit = { shareId -> viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onVaultSelect = {}
    )
}

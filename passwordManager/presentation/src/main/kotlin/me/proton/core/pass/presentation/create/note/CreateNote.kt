package me.proton.core.pass.presentation.create.note

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.common.PassSnackbarHost
import me.proton.core.pass.presentation.components.common.PassSnackbarHostState

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateNote(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val noteUiState by viewModel.noteUiState.collectAsStateWithLifecycle()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { PassSnackbarHostState() }
    val snackbarMessages = NoteSnackbarMessage.values()
        .associateWith { stringResource(id = it.id) }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage
            .collectLatest { message ->
                coroutineScope.launch {
                    snackbarMessages[message]?.let {
                        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                    }
                }
            }
    }
    NoteContent(
        modifier = modifier,
        uiState = noteUiState,
        topBarTitle = R.string.title_create_note,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { shareId -> viewModel.createNote(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        snackbarHost = { PassSnackbarHost(snackbarHostState = snackbarHostState) },
        onSnackbarMessage = { message ->
            coroutineScope.launch {
                snackbarMessages[message]?.let {
                    snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                }
            }
        }
    )
}

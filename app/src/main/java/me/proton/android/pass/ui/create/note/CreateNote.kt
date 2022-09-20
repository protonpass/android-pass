package me.proton.android.pass.ui.create.note

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
internal fun CreateNote(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId
) {
    val viewModel: CreateNoteViewModel = hiltViewModel()
    val viewState by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(viewModel.initialViewState)
    NoteContent(
        viewState = viewState,
        topBarTitle = R.string.title_create_note,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.createNote(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}

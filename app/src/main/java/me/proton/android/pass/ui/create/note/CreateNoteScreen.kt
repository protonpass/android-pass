package me.proton.android.pass.ui.create.note

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.ProtonFormInput
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

internal typealias OnTextChange = (String) -> Unit

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateNoteView(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val viewState by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(viewModel.initialViewState)
    NoteView(
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

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateNoteView(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
    itemId: ItemId,
    viewModel: UpdateNoteViewModel = hiltViewModel()
) {
    viewModel.setItem(shareId, itemId)

    val viewState by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(viewModel.initialViewState)
    NoteView(
        viewState = viewState,
        topBarTitle = R.string.title_edit_note,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}

@ExperimentalComposeUiApi
@Composable
private fun NoteView(
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    viewState: BaseNoteViewModel.ViewState,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    onSubmit: () -> Unit,
    onTitleChange: OnTextChange,
    onNoteChange: OnTextChange
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            onSubmit()
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = stringResource(topBarActionName),
                            color = ProtonTheme.colors.brandNorm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = viewState.state) {
            is BaseNoteViewModel.State.Idle -> CreateNoteItemScreen(
                state = viewState.modelState,
                modifier = Modifier.padding(padding),
                onTitleChange = onTitleChange,
                onNoteChange = onNoteChange
            )
            is BaseNoteViewModel.State.Loading -> DeferredCircularProgressIndicator(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
            is BaseNoteViewModel.State.Error -> Text(text = "something went boom")
            is BaseNoteViewModel.State.Success -> onSuccess(state.itemId)
        }
    }
}

@Composable
private fun CreateNoteItemScreen(
    modifier: Modifier = Modifier,
    state: BaseNoteViewModel.ModelState,
    onTitleChange: OnTextChange,
    onNoteChange: OnTextChange
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TitleInput(value = state.title, onChange = onTitleChange)
        NoteInput(value = state.note, onChange = onNoteChange)
    }
}

@Composable
private fun TitleInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_title_title,
        placeholder = R.string.field_title_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun NoteInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_note_title,
        placeholder = R.string.field_note_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 28.dp),
        singleLine = false,
        moveToNextOnEnter = false
    )
}

package me.proton.pass.presentation.create.alias

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.presentation.R
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.AliasCreated

const val RESULT_CREATED_ALIAS = "created_alias"

@OptIn(
    ExperimentalLifecycleComposeApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun CreateAlias(
    modifier: Modifier = Modifier,
    initialState: InitialCreateAliasUiState? = null,
    onUpClick: () -> Unit,
    onSuccess: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
    LaunchedEffect(initialState) {
        initialState ?: return@LaunchedEffect
        viewModel.setInitialState(initialState)
    }

    val viewState by viewModel.aliasUiState.collectAsStateWithLifecycle()
    val closeScreenState by viewModel.closeScreenEventFlow.collectAsStateWithLifecycle()

    if (closeScreenState is CloseScreenEvent.Close) {
        onClose()
    }

    AliasContent(
        modifier = modifier,
        uiState = viewState,
        topBarTitle = R.string.title_create_alias,
        canEdit = true,
        canDelete = false,
        onUpClick = onUpClick,
        onSuccess = { _, _, alias ->
            viewModel.onEmitSnackbarMessage(AliasCreated)
            onSuccess(alias)
        },
        onSubmit = { shareId -> viewModel.createAlias(shareId) },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxChange = { viewModel.onMailboxChange(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) },
        onDeleteAlias = {} // We cannot delete alias from the Create screen
    )
}

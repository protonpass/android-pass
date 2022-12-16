package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.presentation.R
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage.AliasCreated
import me.proton.pass.presentation.uievents.IsLoadingState

const val RESULT_CREATED_DRAFT_ALIAS = "created_draft_alias"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateAlias(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onAliasCreated: (String) -> Unit,
    onAliasDraftCreated: (AliasItem) -> Unit,
    onClose: () -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
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
        isEditAllowed = viewState.isLoadingState == IsLoadingState.NotLoading,
        onUpClick = onUpClick,
        onAliasCreated = { _, _, alias ->
            viewModel.onEmitSnackbarMessage(AliasCreated)
            onAliasCreated(alias)
        },
        onAliasDraftCreated = { _, aliasItem -> onAliasDraftCreated(aliasItem) },
        onSubmit = { shareId -> viewModel.createAlias(shareId) },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onAliasChange = { viewModel.onAliasChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) },
        onDeleteAlias = {} // We cannot delete alias from the Create screen
    )
}

package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurecreateitem.impl.alias.AliasSnackbarMessage.AliasCreated
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.R

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

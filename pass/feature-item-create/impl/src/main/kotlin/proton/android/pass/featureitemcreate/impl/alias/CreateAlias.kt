package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreateAliasScreen(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onAliasCreated: (String) -> Unit,
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
        topBarActionName = stringResource(id = R.string.title_create_alias),
        canEdit = true,
        isEditAllowed = viewState.isLoadingState == IsLoadingState.NotLoading,
        showVaultSelector = viewState.showVaultSelector,
        onUpClick = onUpClick,
        onAliasCreated = { _, _, alias -> onAliasCreated(alias) },
        onSubmit = { shareId -> viewModel.createAlias(shareId) },
        onSuffixChange = { viewModel.onSuffixChange(it) },
        onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) },
        onPrefixChange = { viewModel.onPrefixChange(it) },
        onVaultSelect = { viewModel.changeVault(it) }
    )
}

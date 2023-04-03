package proton.android.pass.featureitemcreate.impl.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
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
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (viewState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            onUpClick()
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(viewState.closeScreenEvent) {
        if (viewState.closeScreenEvent is CloseScreenEvent.Close) {
            onClose()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = viewState,
            topBarActionName = stringResource(id = R.string.title_create_alias),
            canEdit = true,
            isEditAllowed = viewState.isLoadingState == IsLoadingState.NotLoading,
            showVaultSelector = viewState.showVaultSelector,
            onUpClick = onExit,
            onAliasCreated = { _, _, alias -> onAliasCreated(alias) },
            onSubmit = { shareId -> viewModel.createAlias(shareId) },
            onSuffixChange = { viewModel.onSuffixChange(it) },
            onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onPrefixChange = { viewModel.onPrefixChange(it) },
            onVaultSelect = { viewModel.changeVault(it) }
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = onExit
        )
    }
}

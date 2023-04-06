package proton.android.pass.featureitemcreate.impl.alias

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
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
            onUpClick()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = viewState,
            topBarActionName = stringResource(id = R.string.action_save),
            isCreateMode = false,
            isEditAllowed = viewState.isLoadingState == IsLoadingState.NotLoading,
            showVaultSelector = false,
            onUpClick = onExit,
            onAliasCreated = { shareId, itemId, _ -> onSuccess(shareId, itemId) },
            onSubmit = { viewModel.updateAlias() },
            onSuffixChange = { viewModel.onSuffixChange(it) },
            onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onPrefixChange = { viewModel.onPrefixChange(it) },
            onVaultSelect = {}
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

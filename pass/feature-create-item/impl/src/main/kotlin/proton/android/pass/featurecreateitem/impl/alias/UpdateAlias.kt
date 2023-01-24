package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmMoveItemToTrashDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurecreateitem.impl.R
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
    onAliasDeleted: () -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    val viewState by viewModel.aliasUiState.collectAsStateWithLifecycle()
    val aliasDeletedState by viewModel.aliasDeletedState.collectAsStateWithLifecycle()
    LaunchedEffect(aliasDeletedState) {
        if (aliasDeletedState == ItemDeletedState.Deleted) {
            onAliasDeleted()
        }
    }

    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AliasContent(
            uiState = viewState,
            topBarTitle = R.string.title_edit_alias,
            canEdit = false,
            isUpdate = true,
            isEditAllowed = viewState.isLoadingState == IsLoadingState.NotLoading,
            onUpClick = onUpClick,
            onAliasCreated = { shareId, itemId, _ -> onSuccess(shareId, itemId) },
            onAliasDraftCreated = { _, _ -> },
            onSubmit = { viewModel.updateAlias() },
            onSuffixChange = { viewModel.onSuffixChange(it) },
            onMailboxesChanged = { viewModel.onMailboxesChanged(it) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            onAliasChange = { viewModel.onAliasChange(it) },
            onDeleteAlias = { setShowDeleteDialog(true) },
            onVaultSelect = {}
        )

        ConfirmMoveItemToTrashDialog(
            show = showDeleteDialog,
            itemName = viewState.aliasItem.alias,
            onConfirm = {
                viewModel.onDeleteAlias()
                setShowDeleteDialog(false)
            },
            onDismiss = { setShowDeleteDialog(false) },
            onCancel = { setShowDeleteDialog(false) }
        )
    }
}

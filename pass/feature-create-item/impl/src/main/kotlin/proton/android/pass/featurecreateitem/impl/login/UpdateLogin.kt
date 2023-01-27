package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.dialogs.ConfirmMoveItemToTrashDialog
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.R
import proton.android.pass.featurecreateitem.impl.alias.AliasItem
import proton.android.pass.featurecreateitem.impl.login.LoginSnackbarMessages.LoginUpdated
import proton.android.pass.featurecreateitem.impl.login.bottomsheet.AddTotpType
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun UpdateLogin(
    modifier: Modifier = Modifier,
    draftAlias: AliasItem?,
    primaryTotp: String?,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    onCreateAliasClick: (ShareId, Option<String>) -> Unit,
    onSentToTrash: () -> Unit,
    onAddTotp: (AddTotpType) -> Unit
) {
    val viewModel: UpdateLoginViewModel = hiltViewModel()
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    LaunchedEffect(draftAlias) {
        draftAlias ?: return@LaunchedEffect
        viewModel.setAliasItem(draftAlias)
    }
    LaunchedEffect(primaryTotp) {
        primaryTotp ?: return@LaunchedEffect
        viewModel.setTotp(primaryTotp)
    }

    LaunchedEffect(uiState.isItemSentToTrash) {
        if (uiState.isItemSentToTrash == IsSentToTrashState.Sent) {
            onSentToTrash()
        }
    }

    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }

    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        LoginContent(
            modifier = modifier,
            uiState = uiState,
            showCreateAliasButton = true,
            topBarTitle = R.string.title_edit_login,
            topBarActionName = R.string.action_save,
            isUpdate = true,
            onUpClick = onUpClick,
            onSuccess = { shareId, itemId, _ ->
                viewModel.onEmitSnackbarMessage(LoginUpdated)
                onSuccess(shareId, itemId)
            },
            onSubmit = { shareId -> viewModel.updateItem(shareId) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onUsernameChange = { viewModel.onUsernameChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) },
            onWebsiteChange = onWebsiteChange,
            onNoteChange = { viewModel.onNoteChange(it) },
            onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) },
            onCreateAliasClick = { shareId, titleOption ->
                onCreateAliasClick(shareId, titleOption)
            },
            onRemoveAliasClick = { },
            onDeleteItemClick = { setShowDeleteDialog(true) },
            onVaultSelect = {
                // Migrate element
            },
            onAddTotp = onAddTotp
        )

        ConfirmMoveItemToTrashDialog(
            show = showDeleteDialog,
            itemName = uiState.loginItem.title,
            onConfirm = {
                viewModel.onDelete()
                setShowDeleteDialog(false)
            },
            onDismiss = { setShowDeleteDialog(false) },
            onCancel = { setShowDeleteDialog(false) }
        )
    }
}

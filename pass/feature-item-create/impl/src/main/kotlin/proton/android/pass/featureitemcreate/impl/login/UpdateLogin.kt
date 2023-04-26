package proton.android.pass.featureitemcreate.impl.login

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
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.LoginUpdated
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
    onScanTotp: () -> Unit,
    onCreateAlias: (ShareId, Option<String>) -> Unit,
    onGeneratePasswordClick: () -> Unit
) {
    val viewModel: UpdateLoginViewModel = hiltViewModel()
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.onClose()
            onUpClick()
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(draftAlias) {
        draftAlias ?: return@LaunchedEffect
        viewModel.setAliasItem(draftAlias)
    }
    LaunchedEffect(primaryTotp) {
        primaryTotp ?: return@LaunchedEffect
        viewModel.setTotp(primaryTotp)
    }

    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LoginContent(
            uiState = uiState,
            showCreateAliasButton = true,
            topBarActionName = stringResource(id = R.string.action_save),
            isUpdate = true,
            showVaultSelector = false,
            onUpClick = onExit,
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
            onCreateAlias = onCreateAlias,
            onRemoveAliasClick = { viewModel.onRemoveAlias() },
            onVaultSelect = {
                // Migrate element
            },
            onLinkedAppDelete = viewModel::onDeleteLinkedApp,
            onTotpChange = viewModel::onTotpChange,
            onPasteTotpClick = viewModel::onPasteTotp,
            onScanTotpClick = onScanTotp,
            onGeneratePasswordClick = onGeneratePasswordClick
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                viewModel.onClose()
                onUpClick()
            }
        )
    }
}

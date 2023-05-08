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
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.featureitemcreate.impl.R
import proton.pass.domain.ShareId

@OptIn(
    ExperimentalLifecycleComposeApi::class
)
@Composable
fun CreateLoginScreen(
    modifier: Modifier = Modifier,
    initialContents: InitialCreateLoginUiState? = null,
    showCreateAliasButton: Boolean = true,
    onClose: () -> Unit,
    onSuccess: (ItemUiModel) -> Unit,
    onScanTotp: () -> Unit,
    onCreateAlias: (ShareId, Option<String>) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onUpgrade: () -> Unit,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
    LaunchedEffect(initialContents) {
        initialContents ?: return@LaunchedEffect
        viewModel.setInitialContents(initialContents)
    }
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.onClose()
            onClose()
        }
    }
    BackHandler {
        onExit()
    }

    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LoginContent(
            uiState = uiState,
            showCreateAliasButton = showCreateAliasButton,
            isUpdate = false,
            showVaultSelector = uiState.showVaultSelector,
            topBarActionName = stringResource(id = R.string.title_create_login),
            onUpClick = onExit,
            onSuccess = { _, _, item ->
                viewModel.onEmitSnackbarMessage(LoginSnackbarMessages.LoginCreated)
                onSuccess(item)
            },
            onSubmit = { viewModel.createItem() },
            onTitleChange = { viewModel.onTitleChange(it) },
            onUsernameChange = { viewModel.onUsernameChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) },
            onWebsiteChange = onWebsiteChange,
            onNoteChange = { viewModel.onNoteChange(it) },
            onCreateAlias = onCreateAlias,
            onRemoveAliasClick = { viewModel.onRemoveAlias() },
            onVaultSelect = { viewModel.changeVault(it) },
            onLinkedAppDelete = {},
            onTotpChange = viewModel::onTotpChange,
            onPasteTotpClick = viewModel::onPasteTotp,
            onScanTotpClick = onScanTotp,
            onGeneratePasswordClick = onGeneratePasswordClick,
            onUpgrade = onUpgrade
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                viewModel.onClose()
                onClose()
            }
        )
    }
}

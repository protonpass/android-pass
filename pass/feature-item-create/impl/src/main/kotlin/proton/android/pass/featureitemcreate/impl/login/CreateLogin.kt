package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.featureitemcreate.impl.R

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
    onScanTotp: () -> Unit
) {
    val viewModel: CreateLoginViewModel = hiltViewModel()
    LaunchedEffect(initialContents) {
        initialContents ?: return@LaunchedEffect
        viewModel.setInitialContents(initialContents)
    }

    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()
    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }

    LoginContent(
        modifier = modifier,
        uiState = uiState,
        showCreateAliasButton = showCreateAliasButton,
        isUpdate = false,
        showVaultSelector = uiState.showVaultSelector,
        topBarActionName = stringResource(id = R.string.title_create_login),
        onUpClick = { onClose() },
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
        onAliasCreated = { viewModel.onAliasCreated(it) },
        onRemoveAliasClick = { viewModel.onRemoveAlias() },
        onVaultSelect = { viewModel.changeVault(it) },
        onLinkedAppDelete = {},
        onTotpChange = viewModel::onTotpChange,
        onPasteTotpClick = viewModel::onPasteTotp,
        onScanTotpClick = onScanTotp
    )
}

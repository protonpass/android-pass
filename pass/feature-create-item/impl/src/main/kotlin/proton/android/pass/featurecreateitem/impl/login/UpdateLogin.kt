package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Option
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

    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }

    Box(modifier = modifier) {
        LoginContent(
            modifier = modifier,
            uiState = uiState,
            showCreateAliasButton = true,
            topBarActionName = stringResource(id = R.string.action_save),
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
            onVaultSelect = {
                // Migrate element
            },
            onAddTotp = onAddTotp,
            onDeleteTotp = viewModel::onDeleteTotp,
            onLinkedAppDelete = viewModel::onDeleteLinkedApp
        )
    }
}

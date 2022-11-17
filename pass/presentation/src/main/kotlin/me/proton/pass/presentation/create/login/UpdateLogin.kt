package me.proton.pass.presentation.create.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.create.login.LoginSnackbarMessages.LoginUpdated

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateLogin(
    modifier: Modifier = Modifier,
    createdAlias: String?,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    onCreateAliasClick: (ShareId) -> Unit
) {
    val viewModel: UpdateLoginViewModel = hiltViewModel()
    val uiState by viewModel.loginUiState.collectAsStateWithLifecycle()

    LaunchedEffect(createdAlias) {
        createdAlias?.let { viewModel.onUsernameChange(it) }
    }

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
        topBarTitle = R.string.title_edit_login,
        topBarActionName = R.string.action_save,
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
        onCreateAliasClick = onCreateAliasClick,
        onRemoveAliasClick = { }
    )
}

package me.proton.pass.presentation.create.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.model.ItemUiModel

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateLogin(
    modifier: Modifier = Modifier,
    initialContents: InitialCreateLoginUiState,
    onClose: () -> Unit,
    onSuccess: (ItemUiModel) -> Unit,
    onCreateAliasClick: (ShareId) -> Unit
) {
    val viewModel: CreateLoginViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        viewModel.setInitialContents(initialContents)
    }

    // Necessary for detecting alias generated
    LaunchedEffect(initialContents.username) {
        if (initialContents.username != null) {
            viewModel.onUsernameChange(initialContents.username)
        }
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
        topBarTitle = R.string.title_create_login,
        topBarActionName = R.string.action_save,
        onUpClick = { onClose() },
        onSuccess = { _, _, item -> onSuccess(item) },
        onSubmit = { viewModel.createItem() },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = onWebsiteChange,
        onNoteChange = { viewModel.onNoteChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) },
        onCreateAliasClick = onCreateAliasClick
    )
}

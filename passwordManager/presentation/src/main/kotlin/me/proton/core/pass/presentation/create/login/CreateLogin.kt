package me.proton.core.pass.presentation.create.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.create.login.LoginSnackbarMessages.LoginCreated

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateLogin(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
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
        onUpClick = onUpClick,
        onSuccess = { _, _ ->
            viewModel.onEmitSnackbarMessage(LoginCreated)
            onSuccess()
        },
        onSubmit = { shareId -> viewModel.createItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = onWebsiteChange,
        onNoteChange = { viewModel.onNoteChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) }
    )
}


@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateLoginWithInitialContents(
    modifier: Modifier,
    initialContents: InitialCreateLoginContents,
    onClose: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
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
        topBarTitle = R.string.title_create_login,
        topBarActionName = R.string.action_save,
        onUpClick = { onClose() },
        onSuccess = { _, _ -> onSuccess() },
        onSubmit = { viewModel.createItem() },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = onWebsiteChange,
        onNoteChange = { viewModel.onNoteChange(it) },
        onEmitSnackbarMessage = { viewModel.onEmitSnackbarMessage(it) }
    )
}

package me.proton.pass.presentation.create.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.model.ItemUiModel

@OptIn(
    ExperimentalLifecycleComposeApi::class
)
@Composable
fun CreateLogin(
    modifier: Modifier = Modifier,
    initialContents: InitialCreateLoginUiState? = null,
    showCreateAliasButton: Boolean = true,
    onClose: () -> Unit,
    onSuccess: (ItemUiModel) -> Unit,
    onCreateAliasClick: (ShareId, Option<String>) -> Unit
) {
    val viewModel: CreateLoginViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
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
        onCreateAliasClick = onCreateAliasClick,
        onRemoveAliasClick = { viewModel.onRemoveAlias() }
    )
}

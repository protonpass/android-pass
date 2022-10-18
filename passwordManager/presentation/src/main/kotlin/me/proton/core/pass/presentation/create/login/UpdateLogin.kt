package me.proton.core.pass.presentation.create.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.common.PassSnackbarHostState
import me.proton.core.pass.presentation.create.login.LoginSnackbarMessages.CreationError
import me.proton.core.pass.presentation.create.login.LoginSnackbarMessages.EmptyShareIdError

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateLogin(
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    shareId: ShareId,
    itemId: ItemId,
    viewModel: UpdateLoginViewModel = hiltViewModel()
) {
    viewModel.setItem(shareId, itemId)

    val uiState by viewModel.loginUiState.collectAsState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { PassSnackbarHostState() }

    val creationError = stringResource(id = R.string.create_login_creation_error)
    val emptyShareIdError = stringResource(id = R.string.create_login_empty_share_id)
    val snackbarMessages = mapOf(
        CreationError to creationError,
        EmptyShareIdError to emptyShareIdError
    )
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage
            .collectLatest { message ->
                coroutineScope.launch {
                    snackbarMessages[message]?.let {
                        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                    }
                }
            }
    }
    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }
    LoginContent(
        uiState = uiState,
        topBarTitle = R.string.title_edit_login,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = onWebsiteChange,
        onNoteChange = { viewModel.onNoteChange(it) },
        onSnackbarMessage = { message ->
            coroutineScope.launch {
                snackbarMessages[message]?.let {
                    snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
                }
            }
        }
    )
}

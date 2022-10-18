package me.proton.core.pass.presentation.create.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.common.PassSnackbarHost
import me.proton.core.pass.presentation.components.common.PassSnackbarHostState

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateLogin(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onSuccess: (ShareId, ItemId) -> Unit,
    viewModel: UpdateLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.loginUiState.collectAsState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { PassSnackbarHostState() }
    val snackbarMessages = LoginSnackbarMessages.values()
        .associateWith { stringResource(id = it.id) }
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
    Scaffold(
        snackbarHost = { PassSnackbarHost(snackbarHostState = snackbarHostState) }
    ) { innerPadding ->
        LoginContent(
            modifier = modifier.padding(innerPadding),
            uiState = uiState,
            topBarTitle = R.string.title_edit_login,
            topBarActionName = R.string.action_save,
            onUpClick = onUpClick,
            onSuccess = onSuccess,
            onSubmit = { shareId -> viewModel.updateItem(shareId) },
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
}

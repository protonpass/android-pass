package me.proton.android.pass.ui.create.login

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateLogin(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId
) {
    val viewModel = hiltViewModel<CreateLoginViewModel>()
    val uiState by viewModel.loginUiState.collectAsState()
    val onWebsiteChange = object : OnWebsiteChange {
        override val onWebsiteValueChanged: (String, Int) -> Unit = { value: String, idx: Int ->
            viewModel.onWebsiteChange(value, idx)
        }
        override val onAddWebsite: () -> Unit = { viewModel.onAddWebsite() }
        override val onRemoveWebsite: (Int) -> Unit = { idx: Int -> viewModel.onRemoveWebsite(idx) }
    }
    LoginContent(
        uiState = uiState,
        topBarTitle = R.string.title_create_login,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.createItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = onWebsiteChange,
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}


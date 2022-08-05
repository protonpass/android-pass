package me.proton.android.pass.ui.create.login

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.ProtonFormInput
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.ProtonOutlinedButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.generatePassword

internal typealias OnTextChange = (String) -> Unit

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateLoginView(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
    val viewState by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(viewModel.initialViewState)
    LoginView(
        viewState = viewState,
        topBarTitle = R.string.title_create_login,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.createItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = { viewModel.onWebsiteChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateLoginView(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
    itemId: ItemId,
    viewModel: UpdateLoginViewModel = hiltViewModel()
) {
    viewModel.setItem(shareId, itemId)

    val viewState by rememberFlowWithLifecycle(viewModel.viewState).collectAsState(viewModel.initialViewState)
    LoginView(
        viewState = viewState,
        topBarTitle = R.string.title_edit_login,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = { viewModel.onWebsiteChange(it) },
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}

@ExperimentalComposeUiApi
@Composable
private fun LoginView(
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    viewState: BaseLoginViewModel.ViewState,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    onSubmit: () -> Unit,
    onTitleChange: OnTextChange,
    onUsernameChange: OnTextChange,
    onPasswordChange: OnTextChange,
    onWebsiteChange: OnTextChange,
    onNoteChange: OnTextChange
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            onSubmit()
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = stringResource(topBarActionName),
                            color = ProtonTheme.colors.brandNorm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = viewState.state) {
            is BaseLoginViewModel.State.Idle -> CreateLoginItemScreen(
                state = viewState.modelState,
                modifier = Modifier.padding(padding),
                onTitleChange = onTitleChange,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onWebsiteChange = onWebsiteChange,
                onNoteChange = onNoteChange,
            )
            is BaseLoginViewModel.State.Loading -> DeferredCircularProgressIndicator(Modifier.padding(padding).fillMaxSize())
            is BaseLoginViewModel.State.Error -> Text(text = "something went boom")
            is BaseLoginViewModel.State.Success -> onSuccess(state.itemId)
        }
    }
}

@Composable
private fun CreateLoginItemScreen(
    modifier: Modifier = Modifier,
    state: BaseLoginViewModel.ModelState,
    onTitleChange: OnTextChange,
    onUsernameChange: OnTextChange,
    onPasswordChange: OnTextChange,
    onWebsiteChange: OnTextChange,
    onNoteChange: OnTextChange
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TitleInput(value = state.title, onChange = onTitleChange)
        UsernameInput(value = state.username, onChange = onUsernameChange, onGenerateAliasClick = {})
        PasswordInput(value = state.password, onChange = onPasswordChange)
        Spacer(modifier = Modifier.height(20.dp))
        GeneratePasswordButton(onPasswordGenerated = { onPasswordChange(it) })
        WebsiteAddressInput(value = state.websiteAddress, onChange = onWebsiteChange)
        NoteInput(value = state.note, onChange = onNoteChange)
    }
}

@Composable
private fun TitleInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_title_title,
        placeholder = R.string.field_title_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun UsernameInput(
    value: String,
    onChange: (String) -> Unit,
    onGenerateAliasClick: () -> Unit
) {
    ProtonFormInput(
        title = R.string.field_username_title,
        placeholder = R.string.field_username_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 8.dp),
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_proton_alias),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
                modifier = Modifier.clickable { onGenerateAliasClick() }
            )
        }
    )
}

@Composable
private fun PasswordInput(
    value: String,
    onChange: (String) -> Unit,
) {
    var isVisible: Boolean by rememberSaveable { mutableStateOf(false) }

    val (visualTransformation, icon) = if (isVisible) {
        Pair(VisualTransformation.None, painterResource(R.drawable.ic_proton_eye_slash))
    } else {
        Pair(PasswordVisualTransformation(), painterResource(R.drawable.ic_proton_eye))
    }

    ProtonFormInput(
        title = R.string.field_password_title,
        placeholder = R.string.field_password_hint,
        value = value,
        onChange = onChange,
        visualTransformation = visualTransformation,
        modifier = Modifier.padding(top = 28.dp),
        trailingIcon = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
                modifier = Modifier.clickable { isVisible = !isVisible }
            )
        }
    )
}

@Composable
private fun GeneratePasswordButton(
    onPasswordGenerated: (String) -> Unit
) {
    ProtonOutlinedButton(onClick = { onPasswordGenerated(generatePassword(12)) }) {
        Text(
            text = stringResource(R.string.button_generate_password),
            color = ProtonTheme.colors.brandNorm,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
    }
}

@Composable
private fun WebsiteAddressInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_website_address_title,
        placeholder = R.string.field_website_address_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun NoteInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_note_title,
        placeholder = R.string.field_note_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 28.dp),
        singleLine = false,
        moveToNextOnEnter = false,
    )
}

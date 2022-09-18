package me.proton.android.pass.ui.create.login

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import me.proton.android.pass.ui.create.login.LoginItemValidationErrors.BlankTitle
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.ProtonFormInput
import me.proton.android.pass.ui.shared.ProtonTextField
import me.proton.android.pass.ui.shared.ProtonTextTitle
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.core.compose.component.ProtonOutlinedButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.PasswordGenerator

internal interface OnWebsiteChange {
    val onWebsiteValueChanged: (String, Int) -> Unit
    val onAddWebsite: () -> Unit
    val onRemoveWebsite: (Int) -> Unit
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateLogin(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
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

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateLogin(
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    shareId: ShareId,
    itemId: ItemId,
    viewModel: UpdateLoginViewModel = hiltViewModel()
) {
    viewModel.setItem(shareId, itemId)

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
        topBarTitle = R.string.title_edit_login,
        topBarActionName = R.string.action_save,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateItem(shareId) },
        onTitleChange = { viewModel.onTitleChange(it) },
        onUsernameChange = { viewModel.onUsernameChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onWebsiteChange = onWebsiteChange,
        onNoteChange = { viewModel.onNoteChange(it) }
    )
}

@ExperimentalComposeUiApi
@Composable
private fun LoginContent(
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    uiState: CreateUpdateLoginUiState,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    onSubmit: () -> Unit,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit
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
        if (uiState.isLoadingState == IsLoadingState.Loading) {
            LoadingDialog()
        }
        LoginItemForm(
            loginItem = uiState.loginItem,
            modifier = Modifier.padding(padding),
            onTitleChange = onTitleChange,
            onTitleRequiredError = uiState.errorList.contains(BlankTitle),
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onWebsiteChange = onWebsiteChange,
            onNoteChange = onNoteChange
        )
        LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
            val isItemSaved = uiState.isItemSaved
            if (isItemSaved is ItemSavedState.Success) {
                onSuccess(isItemSaved.itemId)
            }
        }
    }
}

@Composable
private fun LoginItemForm(
    modifier: Modifier = Modifier,
    loginItem: LoginItem,
    onTitleChange: (String) -> Unit,
    onTitleRequiredError: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onWebsiteChange: OnWebsiteChange,
    onNoteChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TitleInput(
            value = loginItem.title,
            onChange = onTitleChange,
            onTitleRequiredError = onTitleRequiredError
        )
        UsernameInput(
            value = loginItem.username,
            onChange = onUsernameChange,
            onGenerateAliasClick = {}
        )
        PasswordInput(value = loginItem.password, onChange = onPasswordChange)
        Spacer(modifier = Modifier.height(20.dp))
        GeneratePasswordButton(onPasswordGenerated = { onPasswordChange(it) })
        WebsitesSection(websites = loginItem.websiteAddresses, onWebsitesChange = onWebsiteChange)
        NoteInput(value = loginItem.note, onChange = onNoteChange)
    }
}

@Composable
private fun WebsitesSection(
    websites: List<String>,
    onWebsitesChange: OnWebsiteChange
) {
    ProtonTextTitle(
        title = R.string.field_website_address_title,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    // Only show the remove button if there is more than 1 website
    val shouldShowRemoveButton = websites.size > 1
    var isFocused: Boolean by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ProtonTheme.colors.backgroundSecondary)
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                shape = RoundedCornerShape(8.dp),
                color = if (isFocused) ProtonTheme.colors.brandNorm else Color.Transparent
            )
    ) {
        val shouldShowAddWebsiteButton =
            (websites.count() == 1 && websites.last().isNotEmpty()) || websites.count() > 1

        websites.forEachIndexed { idx, value ->
            ProtonTextField(
                value = value,
                onChange = { onWebsitesChange.onWebsiteValueChanged(it, idx) },
                onFocusChange = { isFocused = it },
                placeholder = R.string.field_website_address_hint,
                trailingIcon = {
                    if (shouldShowRemoveButton) {
                        Icon(
                            painter = painterResource(R.drawable.ic_proton_minus_circle),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconNorm,
                            modifier = Modifier.clickable { onWebsitesChange.onRemoveWebsite(idx) }
                        )
                    }
                }
            )
            if (shouldShowAddWebsiteButton) {
                Divider()
            }
        }

        AnimatedVisibility(shouldShowAddWebsiteButton) {
            val ableToAddNewWebsite = websites.last().isNotEmpty()
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = ableToAddNewWebsite,
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                contentPadding = PaddingValues(16.dp),
                onClick = { onWebsitesChange.onAddWebsite() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    disabledBackgroundColor = Color.Transparent,
                    contentColor = ProtonTheme.colors.brandNorm,
                    disabledContentColor = ProtonTheme.colors.interactionDisabled
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_proton_plus),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.field_website_add_another))
                Spacer(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TitleInput(value: String, onChange: (String) -> Unit, onTitleRequiredError: Boolean) {
    ProtonFormInput(
        title = R.string.field_title_title,
        placeholder = R.string.field_title_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp),
        isError = onTitleRequiredError
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
    onChange: (String) -> Unit
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
    ProtonOutlinedButton(onClick = { onPasswordGenerated(PasswordGenerator.generatePassword()) }) {
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
private fun NoteInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_note_title,
        placeholder = R.string.field_note_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 28.dp),
        singleLine = false,
        moveToNextOnEnter = false
    )
}

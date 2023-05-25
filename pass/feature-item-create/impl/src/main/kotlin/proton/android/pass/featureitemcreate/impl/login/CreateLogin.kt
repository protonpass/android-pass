package proton.android.pass.featureitemcreate.impl.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.OneTimeLaunchedEffect
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleVaultSelectionSection
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.login.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.login.ShareError.SharesNotAvailable
import proton.pass.domain.ShareId

private enum class CLActionAfterHideKeyboard {
    SelectVault
}

@Suppress("ComplexMethod")
@OptIn(
    ExperimentalLifecycleComposeApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun CreateLoginScreen(
    modifier: Modifier = Modifier,
    initialContents: InitialCreateLoginUiState? = null,
    showCreateAliasButton: Boolean = true,
    clearAlias: Boolean,
    selectVault: ShareId?,
    onNavigate: (BaseLoginNavigation) -> Unit,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
    OneTimeLaunchedEffect(initialContents) {
        initialContents ?: return@OneTimeLaunchedEffect
        viewModel.setInitialContents(initialContents)
    }
    val uiState by viewModel.createLoginUiState.collectAsStateWithLifecycle()
    val keyboardState by keyboardAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var actionWhenKeyboardDisappears by remember { mutableStateOf<CLActionAfterHideKeyboard?>(null) }

    LaunchedEffect(clearAlias) {
        if (clearAlias) {
            viewModel.onRemoveAlias()
        }
    }

    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseLoginUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.onClose()
            onNavigate(BaseLoginNavigation.Close)
        }
    }
    BackHandler {
        onExit()
    }

    val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
        ShareUiState.Loading,
        ShareUiState.NotInitialised -> false to null

        is ShareUiState.Error -> {
            if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                viewModel.onEmitSnackbarMessage(LoginSnackbarMessages.InitError)
                LaunchedEffect(Unit) {
                    onNavigate(BaseLoginNavigation.Close)
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }
    Box(modifier = modifier.fillMaxSize()) {
        LoginContent(
            uiState = uiState.baseLoginUiState,
            selectedShareId = selectedVault?.vault?.shareId,
            showCreateAliasButton = showCreateAliasButton,
            isUpdate = false,
            topBarActionName = stringResource(id = R.string.title_create_login),
            onUpClick = onExit,
            onSuccess = { _, _, item ->
                viewModel.onEmitSnackbarMessage(LoginSnackbarMessages.LoginCreated)
                onNavigate(BaseLoginNavigation.LoginCreated(item))
            },
            onSubmit = { viewModel.createItem() },
            onUsernameChange = viewModel::onUsernameChange,
            onPasswordChange = viewModel::onPasswordChange,
            onWebsiteSectionEvent = {
                when (it) {
                    WebsiteSectionEvent.AddWebsite -> viewModel.onAddWebsite()
                    is WebsiteSectionEvent.RemoveWebsite -> viewModel.onRemoveWebsite(it.index)
                    is WebsiteSectionEvent.WebsiteValueChanged ->
                        viewModel.onWebsiteChange(it.value, it.index)
                }
            },
            onNoteChange = viewModel::onNoteChange,
            onLinkedAppDelete = {},
            onTotpChange = viewModel::onTotpChange,
            onPasteTotpClick = viewModel::onPasteTotp,
            onNavigate = onNavigate,
            titleSection = {
                TitleVaultSelectionSection(
                    titleValue = uiState.baseLoginUiState.loginItem.title,
                    showVaultSelector = showVaultSelector,
                    onTitleChanged = viewModel::onTitleChange,
                    onTitleRequiredError = uiState.baseLoginUiState.validationErrors.contains(
                        LoginItemValidationErrors.BlankTitle
                    ),
                    enabled = uiState.baseLoginUiState.isLoadingState == IsLoadingState.NotLoading,
                    vaultName = selectedVault?.vault?.name,
                    vaultColor = selectedVault?.vault?.color,
                    vaultIcon = selectedVault?.vault?.icon,
                    onVaultClicked = {
                        actionWhenKeyboardDisappears = CLActionAfterHideKeyboard.SelectVault
                        keyboardController?.hide()
                    }
                )
            }
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                viewModel.onClose()
                onNavigate(BaseLoginNavigation.Close)
            }
        )
    }
    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                CLActionAfterHideKeyboard.SelectVault -> {
                    selectedVault ?: return@LaunchedEffect
                    onNavigate(BaseLoginNavigation.SelectVault(selectedVault.vault.shareId))
                    actionWhenKeyboardDisappears = null // Clear flag
                }

                null -> {}
            }
        }
    }
}

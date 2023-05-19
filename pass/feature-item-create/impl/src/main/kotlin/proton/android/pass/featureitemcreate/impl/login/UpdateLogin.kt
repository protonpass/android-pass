package proton.android.pass.featureitemcreate.impl.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.TitleSection
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.LoginUpdated

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun UpdateLogin(
    modifier: Modifier = Modifier,
    draftAlias: AliasItem?,
    primaryTotp: String?,
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    val viewModel: UpdateLoginViewModel = hiltViewModel()
    val uiState by viewModel.updateLoginUiState.collectAsStateWithLifecycle()

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

    LaunchedEffect(draftAlias) {
        draftAlias ?: return@LaunchedEffect
        viewModel.setAliasItem(draftAlias)
    }
    LaunchedEffect(primaryTotp) {
        primaryTotp ?: return@LaunchedEffect
        viewModel.setTotp(primaryTotp)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LoginContent(
            uiState = uiState.baseLoginUiState,
            selectedShareId = uiState.selectedShareId,
            showCreateAliasButton = true,
            topBarActionName = stringResource(id = R.string.action_save),
            isUpdate = true,
            onUpClick = onExit,
            onSuccess = { shareId, itemId, _ ->
                viewModel.onEmitSnackbarMessage(LoginUpdated)
                onNavigate(BaseLoginNavigation.LoginUpdated(shareId, itemId))
            },
            onSubmit = viewModel::updateItem,
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
            onLinkedAppDelete = viewModel::onDeleteLinkedApp,
            onTotpChange = viewModel::onTotpChange,
            onPasteTotpClick = viewModel::onPasteTotp,
            onNavigate = { onNavigate(it) },
            titleSection = {
                TitleSection(
                    modifier = Modifier
                        .roundedContainerNorm()
                        .padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
                    value = uiState.baseLoginUiState.loginItem.title,
                    requestFocus = true,
                    onTitleRequiredError = uiState.baseLoginUiState.validationErrors
                        .contains(LoginItemValidationErrors.BlankTitle),
                    enabled = uiState.baseLoginUiState.isLoadingState == IsLoadingState.NotLoading,
                    isRounded = true,
                    onChange = viewModel::onTitleChange
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
}

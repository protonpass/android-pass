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
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldEvent

@Suppress("ComplexMethod")
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
            onEvent = {
                when (it) {
                    LoginContentEvent.Up -> onExit()
                    is LoginContentEvent.Success -> {
                        viewModel.onEmitSnackbarMessage(LoginUpdated)
                        onNavigate(BaseLoginNavigation.LoginUpdated(it.shareId, it.itemId))
                    }

                    is LoginContentEvent.Submit -> viewModel.updateItem(it.shareId)
                    is LoginContentEvent.OnUsernameChange -> viewModel.onUsernameChange(it.username)
                    is LoginContentEvent.OnPasswordChange -> viewModel.onPasswordChange(it.password)
                    is LoginContentEvent.OnWebsiteEvent -> when (val event = it.event) {
                        WebsiteSectionEvent.AddWebsite -> viewModel.onAddWebsite()
                        is WebsiteSectionEvent.RemoveWebsite -> viewModel.onRemoveWebsite(event.index)
                        is WebsiteSectionEvent.WebsiteValueChanged ->
                            viewModel.onWebsiteChange(event.value, event.index)
                    }

                    is LoginContentEvent.OnNoteChange -> viewModel.onNoteChange(it.note)
                    is LoginContentEvent.OnLinkedAppDelete -> viewModel.onDeleteLinkedApp(it.app)
                    is LoginContentEvent.OnTotpChange -> viewModel.onTotpChange(it.totp)
                    LoginContentEvent.PasteTotp -> viewModel.onPasteTotp()
                    is LoginContentEvent.OnFocusChange ->
                        viewModel.onFocusChange(it.field, it.isFocused)
                    is LoginContentEvent.OnCustomFieldEvent -> {
                        when (val event = it.event) {
                            CustomFieldEvent.AddCustomField -> {
                                onNavigate(BaseLoginNavigation.AddCustomField)
                            }
                            is CustomFieldEvent.OnCustomFieldOptions -> {
                                onNavigate(
                                    BaseLoginNavigation.CustomFieldOptions(
                                        currentValue = event.currentLabel,
                                        index = event.index
                                    )
                                )
                            }
                            is CustomFieldEvent.OnValueChange -> {
                                viewModel.onCustomFieldChange(event.index, event.value)
                            }
                            CustomFieldEvent.Upgrade -> {
                                onNavigate(BaseLoginNavigation.Upgrade)
                            }
                        }
                    }
                }
            },
            onNavigate = { onNavigate(it) },
            titleSection = {
                TitleSection(
                    modifier = Modifier
                        .roundedContainerNorm()
                        .padding(start = 16.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
                    value = uiState.baseLoginUiState.contents.title,
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

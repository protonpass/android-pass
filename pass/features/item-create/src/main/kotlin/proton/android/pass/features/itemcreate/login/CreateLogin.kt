/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.login

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.OneTimeLaunchedEffect
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.dialogs.WarningSharedItemDialog
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.ShareError.EmptyShareList
import proton.android.pass.features.itemcreate.common.ShareError.SharesNotAvailable
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.custom.createupdate.ui.DatePickerModal
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.AddAttachment
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.OnCreateLoginEvent
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.login.LoginField.CustomField
import proton.android.pass.composecomponents.impl.R as CompR

@Suppress("ComplexMethod")
@Composable
fun CreateLoginScreen(
    modifier: Modifier = Modifier,
    initialContents: InitialCreateLoginUiState? = null,
    showCreateAliasButton: Boolean = true,
    clearAlias: Boolean,
    selectVault: ShareId?,
    canUseAttachments: Boolean,
    onNavigate: (BaseLoginNavigation) -> Unit,
    viewModel: CreateLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    OneTimeLaunchedEffect(key = initialContents, saver = InitialCreateLoginUiStateSaver) {
        initialContents ?: return@OneTimeLaunchedEffect
        viewModel.setInitialContents(initialContents)
    }

    LaunchedEffect(Unit) {
        viewModel.duplicateContents(context)
    }

    val uiState by viewModel.createLoginUiState.collectAsStateWithLifecycle()

    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
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
            viewModel.clearDraftData()
            actionAfterKeyboardHide = { onNavigate(BaseLoginNavigation.CloseScreen) }
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
                    actionAfterKeyboardHide = { onNavigate(BaseLoginNavigation.CloseScreen) }
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }


    var showWarningVaultSharedDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LoginContent(
            uiState = uiState.baseLoginUiState,
            passkeyState = uiState.passkeyState,
            loginItemFormState = viewModel.loginItemFormState,
            selectedVault = selectedVault?.vault,
            showVaultSelector = showVaultSelector,
            selectedShareId = selectedVault?.vault?.shareId,
            topBarActionName = stringResource(id = R.string.title_create),
            showCreateAliasButton = showCreateAliasButton,
            canUseAttachments = canUseAttachments,
            isUpdate = false,
            onEvent = {
                when (it) {
                    LoginContentEvent.Up -> onExit()
                    is LoginContentEvent.Submit -> {
                        if (uiState.canDisplayWarningVaultSharedDialog) {
                            showWarningVaultSharedDialog = true
                        } else {
                            viewModel.createItem()
                        }
                    }

                    is LoginContentEvent.OnEmailChanged -> viewModel.onEmailChanged(it.email)
                    is LoginContentEvent.OnUsernameChanged -> viewModel.onUsernameChanged(it.username)
                    is LoginContentEvent.OnPasswordChange -> viewModel.onPasswordChange(it.password)
                    is LoginContentEvent.OnWebsiteEvent -> when (val event = it.event) {
                        WebsiteSectionEvent.AddWebsite -> viewModel.onAddWebsite()
                        is WebsiteSectionEvent.RemoveWebsite -> viewModel.onRemoveWebsite(event.index)
                        is WebsiteSectionEvent.WebsiteValueChanged ->
                            viewModel.onWebsiteChange(event.value, event.index)
                    }

                    is LoginContentEvent.OnNoteChange -> viewModel.onNoteChange(it.note)
                    is LoginContentEvent.OnLinkedAppDelete -> {}
                    is LoginContentEvent.OnTotpChange -> viewModel.onTotpChange(it.totp)
                    LoginContentEvent.PasteTotp -> viewModel.onPasteTotp()
                    is LoginContentEvent.OnFocusChange ->
                        viewModel.onFocusChange(it.field, it.isFocused)

                    is LoginContentEvent.OnCustomFieldEvent -> {
                        when (val event = it.event) {
                            is CustomFieldEvent.OnAddField -> {
                                actionAfterKeyboardHide =
                                    { onNavigate(BaseLoginNavigation.AddCustomField) }
                            }

                            is CustomFieldEvent.OnFieldOptions -> {
                                actionAfterKeyboardHide = {
                                    onNavigate(
                                        CustomFieldOptions(
                                            currentValue = event.label,
                                            index = event.field.index
                                        )
                                    )
                                }
                            }

                            is CustomFieldEvent.OnValueChange -> {
                                viewModel.onCustomFieldChange(event.field, event.value)
                            }

                            CustomFieldEvent.Upgrade -> {
                                actionAfterKeyboardHide =
                                    { onNavigate(BaseLoginNavigation.Upgrade) }
                            }

                            is CustomFieldEvent.FocusRequested ->
                                viewModel.onFocusChange(
                                    field = CustomField(event.field),
                                    isFocused = event.isFocused
                                )

                            is CustomFieldEvent.OnFieldClick -> when (event.field.type) {
                                CustomFieldType.Date -> {
                                    showDatePickerForField = Some(event.field)
                                }

                                else -> throw IllegalStateException("Unhandled action")
                            }
                        }
                    }

                    // Cannot delete passkey from Create Login
                    is LoginContentEvent.OnDeletePasskey -> {}

                    is LoginContentEvent.OnTitleChange -> viewModel.onTitleChange(it.title)

                    is LoginContentEvent.OnVaultSelect ->
                        actionAfterKeyboardHide = {
                            onNavigate(
                                OnCreateLoginEvent(CreateLoginNavigation.SelectVault(it.shareId))
                            )
                        }

                    is LoginContentEvent.OnAliasOptions ->
                        actionAfterKeyboardHide = {
                            onNavigate(
                                BaseLoginNavigation.AliasOptions(
                                    it.shareId,
                                    it.hasReachedAliasLimit
                                )
                            )
                        }

                    is LoginContentEvent.OnCreateAlias ->
                        actionAfterKeyboardHide = {
                            onNavigate(
                                BaseLoginNavigation.CreateAlias(
                                    shareId = it.shareId,
                                    showUpgrade = it.hasReachedAliasLimit,
                                    title = it.title
                                )
                            )
                        }

                    LoginContentEvent.OnCreatePassword ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseLoginNavigation.GeneratePassword) }

                    is LoginContentEvent.OnScanTotp ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseLoginNavigation.ScanTotp(it.index)) }

                    LoginContentEvent.OnUpgrade ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseLoginNavigation.Upgrade) }

                    is LoginContentEvent.OnTooltipDismissed -> {
                        viewModel.onTooltipDismissed(it.tooltip)
                    }

                    LoginContentEvent.OnUsernameOrEmailManuallyExpanded -> {
                        viewModel.onUsernameOrEmailManuallyExpanded()
                    }

                    is LoginContentEvent.OnAttachmentEvent -> {
                        when (val event = it.event) {
                            AttachmentContentEvent.OnAddAttachment -> onNavigate(AddAttachment)
                            is AttachmentContentEvent.OnAttachmentOpen ->
                                throw IllegalStateException("Cannot open attachment during create")

                            is AttachmentContentEvent.OnAttachmentOptions ->
                                onNavigate(
                                    OpenAttachmentOptions(
                                        shareId = event.shareId,
                                        itemId = event.itemId,
                                        attachmentId = event.attachmentId
                                    )
                                )

                            AttachmentContentEvent.OnDeleteAllAttachments ->
                                onNavigate(
                                    DeleteAllAttachments(
                                        uiState.baseLoginUiState.attachmentsState.allToUnlink
                                    )
                                )

                            is AttachmentContentEvent.OnDraftAttachmentOpen ->
                                viewModel.openDraftAttachment(
                                    contextHolder = context.toClassHolder(),
                                    uri = event.uri,
                                    mimetype = event.mimetype
                                )

                            is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                onNavigate(OpenDraftAttachmentOptions(event.uri))

                            is AttachmentContentEvent.OnDraftAttachmentRetry ->
                                viewModel.retryUploadDraftAttachment(event.metadata)

                            AttachmentContentEvent.UpsellAttachments ->
                                onNavigate(BaseLoginNavigation.UpsellAttachments)
                        }
                    }

                    LoginContentEvent.DismissAttachmentBanner ->
                        viewModel.dismissFileAttachmentsOnboardingBanner()
                }
            }
        )

        ConfirmCloseDialog(
            show = showConfirmDialog,
            onCancel = {
                showConfirmDialog = false
            },
            onConfirm = {
                showConfirmDialog = false
                viewModel.clearDraftData()
                actionAfterKeyboardHide = { onNavigate(BaseLoginNavigation.CloseScreen) }
            }
        )
        showDatePickerForField.value()?.let { fieldIdentifier ->
            val selectedDate = viewModel.loginItemFormState
                .customFields[fieldIdentifier.index] as UICustomFieldContent.Date
            DatePickerModal(
                selectedDate = selectedDate.value,
                onDateSelected = {
                    viewModel.onCustomFieldChange(fieldIdentifier, it.toString())
                },
                onDismiss = { showDatePickerForField = None }
            )
        }
    }

    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseLoginUiState.isItemSaved,
        selectedShareId = selectedVault?.vault?.shareId,
        onSuccess = { _, _, model ->
            viewModel.clearDraftData()
            val event = CreateLoginNavigation.LoginCreated(model)
            actionAfterKeyboardHide = { onNavigate(OnCreateLoginEvent(event)) }
        },
        onPasskeyResponse = { response ->
            viewModel.clearDraftData()
            val event = CreateLoginNavigation.LoginCreatedWithPasskey(response)
            actionAfterKeyboardHide = { onNavigate(OnCreateLoginEvent(event)) }
        }
    )

    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseLoginUiState.isItemSaved is ItemSavedState.Success
    )

    if (showWarningVaultSharedDialog) {
        WarningSharedItemDialog(
            description = CompR.string.warning_dialog_item_shared_vault_creating,
            onOkClick = { reminderCheck ->
                showWarningVaultSharedDialog = false
                if (reminderCheck) {
                    viewModel.doNotDisplayWarningDialog()
                }
                viewModel.createItem()
            },
            onCancelClick = {
                showWarningVaultSharedDialog = false
            }
        )
    }
}

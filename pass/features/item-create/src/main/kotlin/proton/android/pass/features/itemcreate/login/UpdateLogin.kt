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
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.custom.createupdate.ui.DatePickerModal
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.AddAttachment
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.AddCustomField
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.login.BaseLoginNavigation.Upgrade
import proton.android.pass.features.itemcreate.login.dialog.ConfirmDeletePasskeyDialog

@Suppress("ComplexMethod")
@Composable
internal fun UpdateLogin(
    modifier: Modifier = Modifier,
    clearAlias: Boolean,
    draftAlias: AliasItemFormState? = null,
    navTotpUri: String? = null,
    navTotpIndex: Int? = null,
    canUseAttachments: Boolean,
    onNavigate: (BaseLoginNavigation) -> Unit,
    viewModel: UpdateLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val uiState by viewModel.updateLoginUiState.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var confirmDeletePasskey: UpdateUiEvent.ConfirmDeletePasskey? by remember { mutableStateOf(null) }
    val onExit = {
        if (uiState.baseLoginUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.clearDraftData()
            actionAfterKeyboardHide = { onNavigate(BaseLoginNavigation.CloseScreen) }
        }
    }

    BackHandler { onExit() }

    LaunchedEffect(draftAlias) {
        draftAlias ?: return@LaunchedEffect
        viewModel.onAliasCreated(draftAlias)
    }

    LaunchedEffect(navTotpUri) {
        navTotpUri ?: return@LaunchedEffect
        viewModel.setTotp(navTotpUri, navTotpIndex ?: -1)
    }

    LaunchedEffect(uiState.uiEvent) {
        when (val event = uiState.uiEvent) {
            UpdateUiEvent.Idle -> {}
            is UpdateUiEvent.ConfirmDeletePasskey -> confirmDeletePasskey = event
        }

        viewModel.consumeEvent(uiState.uiEvent)
    }

    LaunchedEffect(clearAlias) {
        if (clearAlias) {
            viewModel.onRemoveAlias()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LoginContent(
            uiState = uiState.baseLoginUiState,
            passkeyState = None,
            loginItemFormState = viewModel.loginItemFormState,
            selectedVault = null,
            showVaultSelector = false,
            selectedShareId = uiState.selectedShareId,
            topBarActionName = stringResource(id = R.string.action_save),
            showCreateAliasButton = true,
            canUseAttachments = canUseAttachments,
            isUpdate = true,
            onEvent = {
                when (it) {
                    LoginContentEvent.Up -> onExit()
                    is LoginContentEvent.Submit -> viewModel.updateItem(it.shareId)
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
                    is LoginContentEvent.OnLinkedAppDelete -> viewModel.onDeleteLinkedApp(it.app)
                    is LoginContentEvent.OnTotpChange -> viewModel.onTotpChange(it.totp)
                    LoginContentEvent.PasteTotp -> viewModel.onPasteTotp()
                    is LoginContentEvent.OnFocusChange ->
                        viewModel.onFocusChange(it.field, it.isFocused)

                    is LoginContentEvent.OnCustomFieldEvent -> {
                        when (val event = it.event) {
                            is CustomFieldEvent.OnAddField -> {
                                actionAfterKeyboardHide = { onNavigate(AddCustomField) }
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
                                actionAfterKeyboardHide = { onNavigate(Upgrade) }
                            }

                            is CustomFieldEvent.FocusRequested ->
                                viewModel.onFocusChange(
                                    field = LoginField.CustomField(event.field),
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

                    is LoginContentEvent.OnDeletePasskey -> {
                        viewModel.onDeletePasskey(it.idx, it.passkey)
                    }

                    is LoginContentEvent.OnTitleChange ->
                        viewModel.onTitleChange(it.title)

                    is LoginContentEvent.OnVaultSelect -> {}
                    is LoginContentEvent.OnAliasOptions ->
                        actionAfterKeyboardHide = {
                            onNavigate(
                                BaseLoginNavigation.AliasOptions(
                                    it.shareId,
                                    it.hasReachedAliasLimit
                                )
                            )
                        }

                    is LoginContentEvent.OnCreateAlias -> {
                        val shareId = uiState.selectedShareId ?: return@LoginContent
                        actionAfterKeyboardHide = {
                            onNavigate(
                                BaseLoginNavigation.CreateAlias(
                                    shareId = shareId,
                                    showUpgrade = uiState.baseLoginUiState.hasReachedAliasLimit,
                                    title = viewModel.loginItemFormState.title.some()
                                )
                            )
                        }
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
                            AttachmentContentEvent.OnAddAttachment ->
                                onNavigate(AddAttachment)

                            is AttachmentContentEvent.OnAttachmentOpen ->
                                viewModel.openAttachment(
                                    contextHolder = context.toClassHolder(),
                                    attachment = event.attachment
                                )

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
        confirmDeletePasskey?.let { event ->
            ConfirmDeletePasskeyDialog(
                passkey = event.passkey,
                onCancel = {
                    confirmDeletePasskey = null
                },
                onConfirm = {
                    viewModel.onDeletePasskeyConfirmed(event.index, event.passkey)
                    confirmDeletePasskey = null
                }
            )
        }
    }
    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseLoginUiState.isItemSaved,
        selectedShareId = uiState.selectedShareId,
        onSuccess = { shareId, itemId, _ ->
            viewModel.clearDraftData()
            val event = UpdateLoginNavigation.LoginUpdated(shareId, itemId)
            actionAfterKeyboardHide = { onNavigate(BaseLoginNavigation.OnUpdateLoginEvent(event)) }
        }
    )

    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseLoginUiState.isItemSaved is ItemSavedState.Success
    )
}

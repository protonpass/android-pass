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

package proton.android.pass.features.itemcreate.alias

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
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.dialogs.WarningSharedItemDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.composecomponents.impl.R as CompR
import proton.android.pass.features.itemcreate.alias.AliasField.CustomField
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.AddCustomField
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.OnCreateAliasEvent
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.Upgrade
import proton.android.pass.features.itemcreate.alias.CreateAliasNavigation.Created
import proton.android.pass.features.itemcreate.alias.CreateAliasNavigation.SelectVault
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.ShareError.EmptyShareList
import proton.android.pass.features.itemcreate.common.ShareError.SharesNotAvailable
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.custom.createupdate.ui.DatePickerModal
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Composable
fun CreateAliasScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    navTotpUri: String? = null,
    navTotpIndex: Int? = null,
    canUseAttachments: Boolean,
    onNavigate: (BaseAliasNavigation) -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }
    LaunchedEffect(navTotpUri) {
        navTotpUri ?: return@LaunchedEffect
        viewModel.setTotp(navTotpUri, navTotpIndex ?: -1)
    }

    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }

    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )

    val uiState by viewModel.createAliasUiState.collectAsStateWithLifecycle()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseAliasUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.clearDraftData()
            actionAfterKeyboardHide = { onNavigate(BaseAliasNavigation.CloseScreen) }
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(uiState.baseAliasUiState.closeScreenEvent) {
        if (uiState.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            actionAfterKeyboardHide = { onNavigate(BaseAliasNavigation.CloseScreen) }
        }
    }
    val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
        ShareUiState.Loading,
        ShareUiState.NotInitialised -> false to null

        is ShareUiState.Error -> {
            if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                viewModel.onEmitSnackbarMessage(AliasSnackbarMessage.InitError)
                LaunchedEffect(Unit) {
                    actionAfterKeyboardHide = { onNavigate(BaseAliasNavigation.CloseScreen) }
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }

    var showWarningVaultSharedDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = uiState.baseAliasUiState,
            aliasItemFormState = viewModel.aliasItemFormState,
            selectedVault = selectedVault?.vault,
            selectedShareId = selectedVault?.vault?.shareId,
            showVaultSelector = showVaultSelector,
            topBarActionName = stringResource(id = R.string.title_create),
            isCreateMode = true,
            isAliasCreatedByUser = true,
            canUseAttachments = canUseAttachments,
            isEditAllowed = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
            onEvent = { event ->
                when (event) {
                    is AliasContentUiEvent.Back -> onExit()
                    is AliasContentUiEvent.Submit -> {
                        if (uiState.canDisplayWarningVaultSharedDialog) {
                            showWarningVaultSharedDialog = true
                        } else {
                            viewModel.createAlias(event.shareId)
                        }
                    }

                    is AliasContentUiEvent.OnNoteChange -> viewModel.onNoteChange(event.note)
                    is AliasContentUiEvent.OnTitleChange -> viewModel.onTitleChange(event.title)
                    is AliasContentUiEvent.OnVaultSelect ->
                        actionAfterKeyboardHide =
                            { onNavigate(OnCreateAliasEvent(SelectVault(event.shareId))) }

                    is AliasContentUiEvent.OnPrefixChange -> viewModel.onPrefixChange(event.prefix)
                    is AliasContentUiEvent.OnUpgrade ->
                        actionAfterKeyboardHide = { onNavigate(Upgrade) }

                    is AliasContentUiEvent.OnSLNoteChange -> viewModel.onSLNoteChange(event.newSLNote)
                    is AliasContentUiEvent.OnSenderNameChange -> viewModel.onSenderNameChange(event.value)
                    AliasContentUiEvent.OnSlNoteInfoClick -> Unit
                    is AliasContentUiEvent.OnAttachmentEvent -> {
                        when (event.event) {
                            AttachmentContentEvent.OnAddAttachment ->
                                onNavigate(BaseAliasNavigation.AddAttachment)

                            AttachmentContentEvent.OnDeleteAllAttachments -> onNavigate(
                                DeleteAllAttachments(
                                    uiState.baseAliasUiState.attachmentsState.allToUnlink
                                )
                            )

                            is AttachmentContentEvent.OnDraftAttachmentOpen ->
                                viewModel.openDraftAttachment(
                                    contextHolder = context.toClassHolder(),
                                    uri = event.event.uri,
                                    mimetype = event.event.mimetype
                                )

                            is AttachmentContentEvent.OnDraftAttachmentOptions -> onNavigate(
                                OpenDraftAttachmentOptions(event.event.uri)
                            )

                            is AttachmentContentEvent.OnAttachmentOpen,
                            is AttachmentContentEvent.OnAttachmentOptions ->
                                throw IllegalStateException("Action not allowed: $event")

                            is AttachmentContentEvent.OnDraftAttachmentRetry ->
                                viewModel.retryUploadDraftAttachment(event.event.metadata)

                            AttachmentContentEvent.UpsellAttachments ->
                                onNavigate(BaseAliasNavigation.UpsellAttachments)
                        }
                    }

                    AliasContentUiEvent.DismissAttachmentBanner ->
                        viewModel.dismissFileAttachmentsOnboardingBanner()

                    AliasContentUiEvent.DismissAdvancedOptionsBanner ->
                        viewModel.dismissAdvancedOptionsBanner()

                    AliasContentUiEvent.OnMailboxSelect ->
                        onNavigate(BaseAliasNavigation.SelectMailbox)

                    AliasContentUiEvent.OnSuffixSelect ->
                        onNavigate(BaseAliasNavigation.SelectSuffix)

                    is AliasContentUiEvent.OnCustomFieldEvent ->
                        when (val event = event.event) {
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

                    is AliasContentUiEvent.OnScanTotp ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseAliasNavigation.ScanTotp(event.index)) }

                    AliasContentUiEvent.PasteTotp -> viewModel.onPasteTotp()
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
                actionAfterKeyboardHide = { onNavigate(BaseAliasNavigation.CloseScreen) }
            }
        )
        showDatePickerForField.value()?.let { fieldIdentifier ->
            val selectedDate = viewModel.aliasItemFormState
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
        isItemSaved = uiState.baseAliasUiState.itemSavedState,
        selectedShareId = selectedVault?.vault?.shareId,
        onSuccess = { shareId, itemId, model ->
            viewModel.clearDraftData()
            val aliasEmail = (model.contents as ItemContents.Alias).aliasEmail
            val event = BaseAliasNavigation.OnCreateAliasEvent(
                Created(
                    model.userId,
                    shareId,
                    itemId,
                    aliasEmail
                )
            )
            actionAfterKeyboardHide = { onNavigate(event) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseAliasUiState.itemSavedState is ItemSavedState.Success
    )

    selectedVault?.vault?.shareId?.let {
        if (showWarningVaultSharedDialog) {
            WarningSharedItemDialog(
                description = CompR.string.warning_dialog_item_shared_vault_creating,
                onOkClick = { reminderCheck ->
                    showWarningVaultSharedDialog = false
                    if (reminderCheck) {
                        viewModel.doNotDisplayWarningDialog()
                    }
                    viewModel.createAlias(it)
                },
                onCancelClick = {
                    showWarningVaultSharedDialog = false
                }
            )
        }
    }
}

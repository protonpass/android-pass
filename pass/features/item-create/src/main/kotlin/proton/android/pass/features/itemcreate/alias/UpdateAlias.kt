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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.dialogs.PassInfoDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.AddCustomField
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.OnUpdateAliasEvent
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation.Upgrade
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation.Updated
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.custom.createupdate.ui.DatePickerModal
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onNavigate: (BaseAliasNavigation) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }

    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val uiState by viewModel.updateAliasUiState.collectAsStateWithLifecycle()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showSLNoteInfoDialog by rememberSaveable { mutableStateOf(false) }
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

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = uiState.baseAliasUiState,
            aliasItemFormState = viewModel.aliasItemFormState,
            selectedVault = null,
            showVaultSelector = false,
            selectedShareId = uiState.selectedShareId,
            topBarActionName = stringResource(id = R.string.action_save),
            isCreateMode = false,
            isAliasCreatedByUser = uiState.canModify,
            canUseAttachments = true,
            isEditAllowed = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
            onEvent = {
                when (it) {
                    AliasContentUiEvent.Back -> onExit()
                    is AliasContentUiEvent.OnNoteChange -> viewModel.onNoteChange(it.note)
                    is AliasContentUiEvent.OnSLNoteChange -> viewModel.onSLNoteChange(it.newSLNote)
                    is AliasContentUiEvent.OnSenderNameChange ->
                        viewModel.onSenderNameChange(it.value)

                    is AliasContentUiEvent.OnTitleChange -> viewModel.onTitleChange(it.title)
                    AliasContentUiEvent.OnUpgrade ->
                        actionAfterKeyboardHide = { onNavigate(BaseAliasNavigation.Upgrade) }

                    is AliasContentUiEvent.Submit -> viewModel.updateAlias()
                    is AliasContentUiEvent.OnPrefixChange,
                    is AliasContentUiEvent.OnSuffixSelect,
                    is AliasContentUiEvent.OnVaultSelect -> {
                        // Only on create
                    }

                    AliasContentUiEvent.OnSlNoteInfoClick -> {
                        showSLNoteInfoDialog = true
                    }

                    is AliasContentUiEvent.OnAttachmentEvent ->
                        when (val event = it.event) {
                            AttachmentContentEvent.OnAddAttachment ->
                                onNavigate(BaseAliasNavigation.AddAttachment)

                            is AttachmentContentEvent.OnAttachmentOpen ->
                                viewModel.openAttachment(
                                    contextHolder = context.toClassHolder(),
                                    attachment = event.attachment
                                )

                            is AttachmentContentEvent.OnAttachmentOptions ->
                                onNavigate(
                                    OnUpdateAliasEvent(
                                        OpenAttachmentOptions(
                                            shareId = event.shareId,
                                            itemId = event.itemId,
                                            attachmentId = event.attachmentId
                                        )
                                    )
                                )

                            AttachmentContentEvent.OnDeleteAllAttachments ->
                                onNavigate(
                                    DeleteAllAttachments(
                                        uiState.baseAliasUiState.attachmentsState.allToUnlink
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
                                onNavigate(BaseAliasNavigation.UpsellAttachments)
                        }

                    AliasContentUiEvent.DismissAttachmentBanner ->
                        viewModel.dismissFileAttachmentsOnboardingBanner()

                    AliasContentUiEvent.DismissAdvancedOptionsBanner ->
                        viewModel.dismissAdvancedOptionsBanner()

                    AliasContentUiEvent.OnMailboxSelect ->
                        onNavigate(BaseAliasNavigation.SelectMailbox)

                    is AliasContentUiEvent.OnCustomFieldEvent ->
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
                                    field = AliasField.CustomField(event.field),
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
        selectedShareId = uiState.selectedShareId,
        onSuccess = { shareId, itemId, _ ->
            viewModel.clearDraftData()
            actionAfterKeyboardHide = {
                onNavigate(OnUpdateAliasEvent(Updated(shareId, itemId)))
            }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseAliasUiState.itemSavedState is ItemSavedState.Success
    )

    if (showSLNoteInfoDialog) {
        PassInfoDialog(
            title = stringResource(id = R.string.sl_note_info_title),
            message = listOf(
                stringResource(id = R.string.sl_note_info_message_part1),
                stringResource(id = R.string.sl_note_info_message_part2),
                stringResource(id = R.string.sl_note_info_message_part3)
            ).joinToString(separator = SpecialCharacters.SPACE.toString()),
            onDismiss = { showSLNoteInfoDialog = false }
        )
    }
}

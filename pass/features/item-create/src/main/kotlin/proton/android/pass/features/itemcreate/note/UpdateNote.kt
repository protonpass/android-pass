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

package proton.android.pass.features.itemcreate.note

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
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.DialogWarningType
import proton.android.pass.composecomponents.impl.R as CompR
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation.AddCustomField
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.custom.createupdate.ui.DatePickerModal
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide
import proton.android.pass.features.itemcreate.note.NoteField.CustomField
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation.NoteUpdated
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation.OpenAttachmentOptions

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UpdateNote(
    modifier: Modifier = Modifier,
    navTotpUri: String? = null,
    navTotpIndex: Int? = null,
    onNavigate: (BaseNoteNavigation) -> Unit,
    viewModel: UpdateNoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    LaunchedEffect(navTotpUri) {
        navTotpUri ?: return@LaunchedEffect
        viewModel.setTotp(navTotpUri, navTotpIndex ?: -1)
    }
    val noteUiState by viewModel.updateNoteUiState.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (noteUiState.baseNoteUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.clearDraftData()
            actionAfterKeyboardHide = { onNavigate(BaseNoteNavigation.CloseScreen) }
        }
    }
    BackHandler {
        onExit()
    }

    var warningSharedDialog by rememberSaveable { mutableStateOf(DialogWarningType.None) }

    Box(modifier = modifier.fillMaxSize()) {
        NoteContent(
            topBarActionName = stringResource(R.string.action_save),
            uiState = noteUiState.baseNoteUiState,
            selectedVault = null,
            showVaultSelector = false,
            selectedShareId = noteUiState.selectedShareId,
            noteItemFormState = viewModel.noteItemFormState,
            onEvent = {
                when (it) {
                    NoteContentUiEvent.Back -> onExit()
                    NoteContentUiEvent.Upgrade ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseNoteNavigation.Upgrade) }

                    is NoteContentUiEvent.OnNoteChange -> viewModel.onNoteChange(it.note)
                    is NoteContentUiEvent.OnTitleChange -> viewModel.onTitleChange(it.title)
                    is NoteContentUiEvent.OnVaultSelect -> {}
                    is NoteContentUiEvent.Submit -> {
                        when {
                            noteUiState.canDisplaySharedItemWarningDialog -> {
                                warningSharedDialog = DialogWarningType.SharedItem
                            }

                            noteUiState.canDisplayVaultSharedWarningDialog -> {
                                warningSharedDialog = DialogWarningType.SharedVault
                            }

                            else -> {
                                warningSharedDialog = DialogWarningType.None
                                viewModel.updateItem(it.shareId)
                            }
                        }
                    }

                    is NoteContentUiEvent.OnAttachmentEvent ->
                        actionAfterKeyboardHide =
                            {
                                when (val event = it.event) {
                                    AttachmentContentEvent.OnAddAttachment ->
                                        onNavigate(BaseNoteNavigation.AddAttachment)

                                    is AttachmentContentEvent.OnAttachmentOpen ->
                                        viewModel.onAttachmentOpen(
                                            contextHolder = context.toClassHolder(),
                                            attachment = event.attachment
                                        )

                                    is AttachmentContentEvent.OnAttachmentOptions ->
                                        onNavigate(
                                            BaseNoteNavigation.OnUpdateNoteEvent(
                                                OpenAttachmentOptions(
                                                    shareId = event.shareId,
                                                    itemId = event.itemId,
                                                    attachmentId = event.attachmentId
                                                )
                                            )
                                        )

                                    AttachmentContentEvent.OnDeleteAllAttachments ->
                                        onNavigate(
                                            BaseNoteNavigation.DeleteAllAttachments(
                                                noteUiState.baseNoteUiState.attachmentsState.allToUnlink
                                            )
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentOpen ->
                                        viewModel.openDraftAttachment(
                                            contextHolder = context.toClassHolder(),
                                            uri = event.uri,
                                            mimetype = event.mimetype
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                        onNavigate(
                                            BaseNoteNavigation.OpenDraftAttachmentOptions(event.uri)
                                        )

                                    is AttachmentContentEvent.OnDraftAttachmentRetry ->
                                        viewModel.retryUploadDraftAttachment(event.metadata)

                                    AttachmentContentEvent.UpsellAttachments ->
                                        onNavigate(BaseNoteNavigation.UpsellAttachments)
                                }
                            }

                    is NoteContentUiEvent.OnCustomFieldEvent -> {
                        when (val cevent = it.event) {
                            is CustomFieldEvent.OnAddField -> {
                                actionAfterKeyboardHide = {
                                    onNavigate(
                                        BaseNoteNavigation.NoteCustomFieldNavigation(AddCustomField)
                                    )
                                }
                            }

                            is CustomFieldEvent.OnFieldOptions -> {
                                actionAfterKeyboardHide = {
                                    onNavigate(
                                        BaseNoteNavigation.NoteCustomFieldNavigation(
                                            CustomFieldOptions(
                                                currentValue = cevent.label,
                                                index = cevent.field.index
                                            )
                                        )
                                    )
                                }
                            }

                            is CustomFieldEvent.OnValueChange -> {
                                viewModel.onCustomFieldChange(cevent.field, cevent.value)
                            }

                            CustomFieldEvent.Upgrade ->
                                actionAfterKeyboardHide =
                                    { onNavigate(BaseNoteNavigation.Upgrade) }

                            is CustomFieldEvent.FocusRequested ->
                                viewModel.onFocusChange(
                                    field = CustomField(cevent.field),
                                    isFocused = cevent.isFocused
                                )

                            is CustomFieldEvent.OnFieldClick -> when (cevent.field.type) {
                                CustomFieldType.Date -> {
                                    showDatePickerForField = Some(cevent.field)
                                }

                                else -> throw IllegalStateException("Unhandled action")
                            }
                        }
                    }

                    NoteContentUiEvent.DismissAttachmentBanner ->
                        viewModel.dismissFileAttachmentsOnboardingBanner()

                    is NoteContentUiEvent.OnScanTotp ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseNoteNavigation.ScanTotp(it.index)) }

                    NoteContentUiEvent.PasteTotp -> viewModel.onPasteTotp()
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
                actionAfterKeyboardHide = { onNavigate(BaseNoteNavigation.CloseScreen) }
            }
        )
        showDatePickerForField.value()?.let { fieldIdentifier ->
            val selectedDate = viewModel.noteItemFormState
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
        isItemSaved = noteUiState.baseNoteUiState.itemSavedState,
        selectedShareId = noteUiState.selectedShareId,
        onSuccess = { shareId, itemId, _ ->
            viewModel.clearDraftData()
            actionAfterKeyboardHide =
                { onNavigate(BaseNoteNavigation.OnUpdateNoteEvent(NoteUpdated(shareId, itemId))) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = noteUiState.baseNoteUiState.itemSavedState is ItemSavedState.Success
    )

    noteUiState.selectedShareId?.let {
        if (warningSharedDialog != DialogWarningType.None) {
            WarningSharedItemDialog(
                title = when (warningSharedDialog) {
                    DialogWarningType.SharedVault -> CompR.string.warning_dialog_item_shared_vault_title
                    DialogWarningType.SharedItem -> CompR.string.warning_dialog_item_shared_title
                    else -> throw IllegalStateException("Unhandled case")
                },
                description = when (warningSharedDialog) {
                    DialogWarningType.SharedVault -> CompR.string.warning_dialog_item_shared_vault_updating
                    DialogWarningType.SharedItem -> CompR.string.warning_dialog_item_shared_updating
                    else -> throw IllegalStateException("Unhandled case")
                },
                onOkClick = { reminderCheck ->
                    warningSharedDialog = DialogWarningType.None
                    if (reminderCheck) {
                        viewModel.doNotDisplayWarningDialog()
                    }
                    viewModel.updateItem(shareId = it)
                },
                onCancelClick = {
                    warningSharedDialog = DialogWarningType.None
                }
            )
        }
    }
}

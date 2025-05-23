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
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.dialogs.PassInfoDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation.AddAttachment
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.alias.UpdateAliasNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onNavigate: (UpdateAliasNavigation) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
            actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.CloseScreen) }
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(uiState.baseAliasUiState.closeScreenEvent) {
        if (uiState.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.CloseScreen) }
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
                        actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.Upgrade) }

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
                                onNavigate(UpdateAliasNavigation.UpsellAttachments)
                        }

                    AliasContentUiEvent.DismissAttachmentBanner ->
                        viewModel.dismissFileAttachmentsOnboardingBanner()

                    AliasContentUiEvent.DismissAdvancedOptionsBanner ->
                        viewModel.dismissAdvancedOptionsBanner()

                    AliasContentUiEvent.OnMailboxSelect ->
                        onNavigate(UpdateAliasNavigation.SelectMailbox)
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
                actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.CloseScreen) }
            }
        )
    }
    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseAliasUiState.itemSavedState,
        selectedShareId = uiState.selectedShareId,
        onSuccess = { shareId, itemId, _ ->
            viewModel.clearDraftData()
            actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.Updated(shareId, itemId)) }
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

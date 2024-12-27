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
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation.AddAttachment
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.note.UpdateNoteNavigation.OpenDraftAttachmentOptions

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UpdateNote(
    modifier: Modifier = Modifier,
    onNavigate: (UpdateNoteNavigation) -> Unit,
    viewModel: UpdateNoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )

    val noteUiState by viewModel.updateNoteUiState.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (noteUiState.baseNoteUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            actionAfterKeyboardHide = { onNavigate(UpdateNoteNavigation.Back) }
        }
    }
    BackHandler {
        onExit()
    }

    Box(modifier = modifier.fillMaxSize()) {
        NoteContent(
            topBarActionName = stringResource(R.string.action_save),
            uiState = noteUiState.baseNoteUiState,
            selectedVault = null,
            showVaultSelector = false,
            selectedShareId = noteUiState.selectedShareId,
            noteItemFormState = viewModel.noteItemFormState,
            onEvent = { event ->
                when (event) {
                    NoteContentUiEvent.Back -> onExit()
                    is NoteContentUiEvent.OnNoteChange -> viewModel.onNoteChange(event.note)
                    is NoteContentUiEvent.OnTitleChange -> viewModel.onTitleChange(event.title)
                    is NoteContentUiEvent.OnVaultSelect -> {}
                    is NoteContentUiEvent.Submit -> viewModel.updateItem(event.shareId)
                    is NoteContentUiEvent.OnAttachmentEvent ->
                        actionAfterKeyboardHide =
                            {
                                when (event.event) {
                                    AttachmentContentEvent.OnAddAttachment ->
                                        onNavigate(AddAttachment)
                                    is AttachmentContentEvent.OnAttachmentOpen ->
                                        viewModel.onAttachmentOpen(
                                            contextHolder = context.toClassHolder(),
                                            attachment = event.event.attachment
                                        )
                                    is AttachmentContentEvent.OnAttachmentOptions ->
                                        onNavigate(OpenAttachmentOptions(event.event.attachmentId))
                                    AttachmentContentEvent.OnDeleteAllAttachments ->
                                        onNavigate(DeleteAllAttachments)
                                    is AttachmentContentEvent.OnDraftAttachmentOpen ->
                                        viewModel.openDraftAttachment(
                                            contextHolder = context.toClassHolder(),
                                            uri = event.event.uri,
                                            mimetype = event.event.mimetype
                                        )
                                    is AttachmentContentEvent.OnDraftAttachmentOptions ->
                                        onNavigate(OpenDraftAttachmentOptions(event.event.uri))
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
                actionAfterKeyboardHide = { onNavigate(UpdateNoteNavigation.Back) }
            }
        )
    }
    ItemSavedLaunchedEffect(
        isItemSaved = noteUiState.baseNoteUiState.itemSavedState,
        selectedShareId = noteUiState.selectedShareId,
        onSuccess = { shareId, itemId, _ ->
            actionAfterKeyboardHide =
                { onNavigate(UpdateNoteNavigation.NoteUpdated(shareId, itemId)) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = noteUiState.baseNoteUiState.itemSavedState is ItemSavedState.Success
    )
}

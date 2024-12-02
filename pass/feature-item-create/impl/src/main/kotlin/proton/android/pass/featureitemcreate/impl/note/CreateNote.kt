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

package proton.android.pass.featureitemcreate.impl.note

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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.common.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.common.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.common.attachments.AttachmentContentEvent
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.featureitemcreate.impl.login.PerformActionAfterKeyboardHide

@Suppress("ComplexMethod")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateNoteScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    onNavigate: (CreateNoteNavigation) -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.changeVault(selectVault)
        }
    }
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }

    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )

    val uiState by viewModel.createNoteUiState.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseNoteUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            actionAfterKeyboardHide = { onNavigate(CreateNoteNavigation.Back) }
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
                viewModel.onEmitSnackbarMessage(NoteSnackbarMessage.InitError)
                LaunchedEffect(Unit) {
                    onNavigate(CreateNoteNavigation.Back)
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NoteContent(
            uiState = uiState.baseNoteUiState,
            noteItemFormState = viewModel.noteItemFormState,
            selectedVault = selectedVault?.vault,
            showVaultSelector = showVaultSelector,
            selectedShareId = selectedVault?.vault?.shareId,
            topBarActionName = stringResource(R.string.title_create),
            onEvent = { event ->
                when (event) {
                    NoteContentUiEvent.Back -> onExit()
                    is NoteContentUiEvent.Submit -> viewModel.createNote(event.shareId)
                    is NoteContentUiEvent.OnVaultSelect ->
                        actionAfterKeyboardHide =
                            { onNavigate(CreateNoteNavigation.SelectVault(event.shareId)) }

                    is NoteContentUiEvent.OnNoteChange -> viewModel.onNoteChange(event.note)
                    is NoteContentUiEvent.OnTitleChange -> viewModel.onTitleChange(event.title)
                    is NoteContentUiEvent.OnAttachmentEvent ->
                        actionAfterKeyboardHide =
                            {
                                when (event.event) {
                                    AttachmentContentEvent.OnAddAttachment ->
                                        onNavigate(CreateNoteNavigation.AddAttachment)

                                    is AttachmentContentEvent.OnAttachmentOpen -> {

                                    }
                                    is AttachmentContentEvent.OnAttachmentOptions -> {

                                    }
                                    AttachmentContentEvent.OnDeleteAllAttachments -> {
                                        // cannot delete all attachments in notes
                                    }
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
                actionAfterKeyboardHide = { onNavigate(CreateNoteNavigation.Back) }
            }
        )
    }
    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseNoteUiState.itemSavedState,
        selectedShareId = selectedVault?.vault?.shareId,
        onSuccess = { _, _, _ ->
            actionAfterKeyboardHide = { onNavigate(CreateNoteNavigation.NoteCreated) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseNoteUiState.itemSavedState is ItemSavedState.Success
    )
}

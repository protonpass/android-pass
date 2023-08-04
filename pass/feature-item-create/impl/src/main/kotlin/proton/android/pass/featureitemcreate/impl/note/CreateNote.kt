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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.form.VaultSelector
import proton.android.pass.composecomponents.impl.keyboard.keyboardAsState
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.common.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.common.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.note.CNActionAfterHideKeyboard.SelectVault
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId

private enum class CNActionAfterHideKeyboard {
    SelectVault
}

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

    val uiState by viewModel.createNoteUiState.collectAsStateWithLifecycle()
    val keyboardState by keyboardAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var actionWhenKeyboardDisappears by remember { mutableStateOf<CNActionAfterHideKeyboard?>(null) }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseNoteUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            onNavigate(CreateNoteNavigation.Back)
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
            selectedShareId = selectedVault?.vault?.shareId,
            topBarActionName = stringResource(R.string.title_create_note),
            onUpClick = onExit,
            onSubmit = { shareId -> viewModel.createNote(shareId) },
            onTitleChange = { viewModel.onTitleChange(it) },
            onNoteChange = { viewModel.onNoteChange(it) },
            vaultSelect = {
                if (showVaultSelector) {
                    Column { // Column so spacedBy does not affect the spacer
                        VaultSelector(
                            modifier = Modifier.roundedContainerNorm(),
                            vaultName = selectedVault?.vault?.name ?: "",
                            color = selectedVault?.vault?.color ?: ShareColor.Color1,
                            icon = selectedVault?.vault?.icon ?: ShareIcon.Icon1,
                            onVaultClicked = {
                                actionWhenKeyboardDisappears = SelectVault
                                keyboardController?.hide()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // 16 come from spacedBy + 8 = 24
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
                onNavigate(CreateNoteNavigation.Back)
            }
        )
    }

    LaunchedEffect(keyboardState, actionWhenKeyboardDisappears) {
        if (!keyboardState) {
            when (actionWhenKeyboardDisappears) {
                SelectVault -> {
                    selectedVault ?: return@LaunchedEffect
                    onNavigate(
                        CreateNoteNavigation.SelectVault(selectedVault.vault.shareId)
                    )
                    actionWhenKeyboardDisappears = null // Clear flag
                }

                null -> {}
            }
        }
    }
    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseNoteUiState.itemSavedState,
        selectedShareId = selectedVault?.vault?.shareId,
        onSuccess = { _, _, _ ->
            onNavigate(CreateNoteNavigation.NoteCreated)
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseNoteUiState.itemSavedState is ItemSavedState.Success,
    )
}

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

package proton.android.pass.featureitemcreate.impl.alias

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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.dialogs.PassInfoDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.featureitemcreate.impl.login.PerformActionAfterKeyboardHide

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAlias(
    modifier: Modifier = Modifier,
    onNavigate: (UpdateAliasNavigation) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
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
            actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.Close) }
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(uiState.baseAliasUiState.closeScreenEvent) {
        if (uiState.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.Close) }
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
            isEditAllowed = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
            onEvent = { event ->
                when (event) {
                    AliasContentUiEvent.Back -> onExit()
                    is AliasContentUiEvent.OnMailBoxChanged -> viewModel.onMailboxesChanged(event.list)
                    is AliasContentUiEvent.OnNoteChange -> viewModel.onNoteChange(event.note)
                    is AliasContentUiEvent.OnSLNoteChange -> viewModel.onSLNoteChange(event.newSLNote)
                    is AliasContentUiEvent.OnSenderNameChange ->
                        viewModel.onSenderNameChange(event.value)

                    is AliasContentUiEvent.OnTitleChange -> viewModel.onTitleChange(event.title)
                    AliasContentUiEvent.OnUpgrade ->
                        actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.Upgrade) }

                    is AliasContentUiEvent.Submit -> viewModel.updateAlias()
                    is AliasContentUiEvent.OnPrefixChange,
                    is AliasContentUiEvent.OnSuffixChanged,
                    is AliasContentUiEvent.OnVaultSelect -> {
                        // Only on create
                    }

                    AliasContentUiEvent.OnSlNoteInfoClick -> {
                        showSLNoteInfoDialog = true
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
                actionAfterKeyboardHide = { onNavigate(UpdateAliasNavigation.Close) }
            }
        )
    }
    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseAliasUiState.itemSavedState,
        selectedShareId = uiState.selectedShareId,
        onSuccess = { shareId, itemId, _ ->
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

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.common.ShareError.EmptyShareList
import proton.android.pass.featureitemcreate.impl.common.ShareError.SharesNotAvailable
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.featureitemcreate.impl.login.PerformActionAfterKeyboardHide

@Composable
fun CreateAliasScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    onNavigate: (CreateAliasNavigation) -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
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

    val uiState by viewModel.createAliasUiState.collectAsStateWithLifecycle()
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (uiState.baseAliasUiState.hasUserEditedContent) {
            showConfirmDialog = !showConfirmDialog
        } else {
            actionAfterKeyboardHide = { onNavigate(CreateAliasNavigation.Close) }
        }
    }
    BackHandler {
        onExit()
    }

    LaunchedEffect(uiState.baseAliasUiState.closeScreenEvent) {
        if (uiState.baseAliasUiState.closeScreenEvent is CloseScreenEvent.Close) {
            actionAfterKeyboardHide = { onNavigate(CreateAliasNavigation.Close) }
        }
    }
    val (showVaultSelector, selectedVault) = when (val shares = uiState.shareUiState) {
        ShareUiState.Loading,
        ShareUiState.NotInitialised -> false to null

        is ShareUiState.Error -> {
            if (shares.shareError == EmptyShareList || shares.shareError == SharesNotAvailable) {
                viewModel.onEmitSnackbarMessage(AliasSnackbarMessage.InitError)
                LaunchedEffect(Unit) {
                    actionAfterKeyboardHide = { onNavigate(CreateAliasNavigation.Close) }
                }
            }
            false to null
        }

        is ShareUiState.Success -> (shares.vaultList.size > 1) to shares.currentVault
    }
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AliasContent(
            uiState = uiState.baseAliasUiState,
            aliasItemFormState = viewModel.aliasItemFormState,
            selectedVault = selectedVault?.vault,
            showVaultSelector = showVaultSelector,
            selectedShareId = selectedVault?.vault?.shareId,
            topBarActionName = stringResource(id = R.string.title_create),
            isCreateMode = true,
            isEditAllowed = uiState.baseAliasUiState.isLoadingState == IsLoadingState.NotLoading,
            onEvent = { event ->
                when (event) {
                    is AliasContentUiEvent.Back -> onExit()
                    is AliasContentUiEvent.Submit -> viewModel.createAlias(event.shareId)
                    is AliasContentUiEvent.OnNoteChange -> viewModel.onNoteChange(event.note)
                    is AliasContentUiEvent.OnTitleChange -> viewModel.onTitleChange(event.title)
                    is AliasContentUiEvent.OnVaultSelect ->
                        actionAfterKeyboardHide =
                            { onNavigate(CreateAliasNavigation.SelectVault(event.shareId)) }

                    is AliasContentUiEvent.OnPrefixChange -> viewModel.onPrefixChange(event.prefix)
                    is AliasContentUiEvent.OnMailBoxChanged -> viewModel.onMailboxesChanged(event.list)
                    is AliasContentUiEvent.OnSuffixChanged -> viewModel.onSuffixChange(event.suffix)
                    is AliasContentUiEvent.OnUpgrade ->
                        actionAfterKeyboardHide = { onNavigate(CreateAliasNavigation.Upgrade) }
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
                actionAfterKeyboardHide = { onNavigate(CreateAliasNavigation.Close) }
            }
        )
    }

    ItemSavedLaunchedEffect(
        isItemSaved = uiState.baseAliasUiState.itemSavedState,
        selectedShareId = selectedVault?.vault?.shareId,
        onSuccess = { shareId, itemId, model ->
            val aliasEmail = (model.contents as ItemContents.Alias).aliasEmail
            val event = CreateAliasNavigation.Created(shareId, itemId, aliasEmail)
            actionAfterKeyboardHide = { onNavigate(event) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = uiState.baseAliasUiState.itemSavedState is ItemSavedState.Success
    )
}

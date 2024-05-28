/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featureitemcreate.impl.identity.ui

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
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.CreateIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.navigation.bottomsheets.AddIdentityFieldType
import proton.android.pass.featureitemcreate.impl.identity.presentation.CreateIdentityViewModel
import proton.android.pass.featureitemcreate.impl.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.featureitemcreate.impl.login.PerformActionAfterKeyboardHide

@Composable
fun CreateIdentityScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateIdentityViewModel = hiltViewModel(),
    onNavigate: (BaseIdentityNavigation) -> Unit
) {
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.onVaultSelect(selectVault)
        }
    }

    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val onExit = {
        if (state.hasUserEdited) {
            showConfirmDialog = !showConfirmDialog
        } else {
            actionAfterKeyboardHide = { onNavigate(BaseIdentityNavigation.Close) }
        }
    }
    BackHandler(onBack = onExit)
    Box(modifier = modifier.fillMaxSize()) {
        CreateIdentityContent(
            identityItemFormState = viewModel.getFormState(),
            selectedVault = state.getSelectedVault(),
            isLoadingState = state.getSubmitLoadingState(),
            shouldShowVaultSelector = state.shouldShowVaultSelector(),
            validationErrors = state.getValidationErrors(),
            extraFields = state.getExtraFields(),
            topBarActionName = stringResource(id = R.string.title_create),
            onEvent = { event ->
                when (event) {
                    is IdentityContentEvent.OnVaultSelect ->
                        actionAfterKeyboardHide =
                            { onNavigate(CreateIdentityNavigation.SelectVault(event.shareId)) }

                    is IdentityContentEvent.Submit -> viewModel.onSubmit(event.shareId)
                    IdentityContentEvent.Up -> onExit()

                    is IdentityContentEvent.OnFieldChange -> viewModel.onFieldChange(event.value)

                    IdentityContentEvent.OnAddAddressDetailField ->
                        onNavigate(BaseIdentityNavigation.OpenExtraFieldBottomSheet(AddIdentityFieldType.Address))

                    IdentityContentEvent.OnAddContactDetailField ->
                        onNavigate(BaseIdentityNavigation.OpenExtraFieldBottomSheet(AddIdentityFieldType.Contact))

                    IdentityContentEvent.OnAddPersonalDetailField ->
                        onNavigate(BaseIdentityNavigation.OpenExtraFieldBottomSheet(AddIdentityFieldType.Personal))

                    IdentityContentEvent.OnAddWorkField ->
                        onNavigate(BaseIdentityNavigation.OpenExtraFieldBottomSheet(AddIdentityFieldType.Work))

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
                actionAfterKeyboardHide = { onNavigate(BaseIdentityNavigation.Close) }
            }
        )
    }
    ItemSavedLaunchedEffect(
        isItemSaved = state.getItemSavedState(),
        selectedShareId = state.getSelectedVault().value()?.shareId,
        onSuccess = { _, _, model ->
            actionAfterKeyboardHide =
                { onNavigate(CreateIdentityNavigation.ItemCreated(model)) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = state.getItemSavedState() is ItemSavedState.Success
    )
}

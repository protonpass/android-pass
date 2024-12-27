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

package proton.android.pass.features.itemcreate.identity.ui

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
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.AddAttachment
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.AddExtraSection
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.CustomFieldOptions
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.DeleteAllAttachments
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.ExtraSectionOptions
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.OpenAttachmentOptions
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.OpenDraftAttachmentOptions
import proton.android.pass.features.itemcreate.identity.navigation.BaseIdentityNavigation.OpenExtraFieldBottomSheet
import proton.android.pass.features.itemcreate.identity.navigation.CreateIdentityNavigation
import proton.android.pass.features.itemcreate.identity.navigation.CreateIdentityNavigation.SelectVault
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.AddIdentityFieldType
import proton.android.pass.features.itemcreate.identity.presentation.CreateIdentityViewModel
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Composable
fun CreateIdentityScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateIdentityViewModel = hiltViewModel(),
    onNavigate: (BaseIdentityNavigation) -> Unit
) {
    val context = LocalContext.current
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
        IdentityContent(
            identityItemFormState = viewModel.getFormState(),
            identityUiState = state,
            topBarActionName = stringResource(id = R.string.title_create),
            onEvent = { event ->
                when (event) {
                    is IdentityContentEvent.OnVaultSelect ->
                        actionAfterKeyboardHide = { onNavigate(SelectVault(event.shareId)) }

                    is IdentityContentEvent.Submit -> viewModel.onSubmit(event.shareId)
                    IdentityContentEvent.Up -> onExit()

                    is IdentityContentEvent.OnFieldChange -> viewModel.onFieldChange(event.value)

                    IdentityContentEvent.OnAddAddressDetailField -> actionAfterKeyboardHide = {
                        onNavigate(OpenExtraFieldBottomSheet(AddIdentityFieldType.Address))
                    }

                    IdentityContentEvent.OnAddContactDetailField -> actionAfterKeyboardHide = {
                        onNavigate(OpenExtraFieldBottomSheet(AddIdentityFieldType.Contact))
                    }

                    IdentityContentEvent.OnAddPersonalDetailField -> actionAfterKeyboardHide = {
                        onNavigate(OpenExtraFieldBottomSheet(AddIdentityFieldType.Personal))
                    }

                    IdentityContentEvent.OnAddWorkField -> actionAfterKeyboardHide = {
                        onNavigate(OpenExtraFieldBottomSheet(AddIdentityFieldType.Work))
                    }

                    is IdentityContentEvent.OnCustomFieldOptions -> {
                        viewModel.updateSelectedSection(event.customExtraField)
                        actionAfterKeyboardHide = {
                            onNavigate(CustomFieldOptions(event.label, event.index))
                        }
                    }

                    IdentityContentEvent.OnAddExtraSection -> {
                        actionAfterKeyboardHide = { onNavigate(AddExtraSection) }
                    }

                    is IdentityContentEvent.OnAddExtraSectionCustomField -> actionAfterKeyboardHide = {
                        onNavigate(OpenExtraFieldBottomSheet(AddIdentityFieldType.Extra, event.index.some()))
                    }

                    is IdentityContentEvent.OnExtraSectionOptions ->
                        actionAfterKeyboardHide = {
                            onNavigate(ExtraSectionOptions(event.label, event.index))
                        }

                    IdentityContentEvent.ClearLastAddedFieldFocus ->
                        viewModel.resetLastAddedFieldFocus()

                    is IdentityContentEvent.OnCustomFieldFocused ->
                        viewModel.onCustomFieldFocusChange(event.index, event.isFocused, event.customExtraField)

                    is IdentityContentEvent.OnAttachmentEvent -> when (event.event) {
                        AttachmentContentEvent.OnAddAttachment -> onNavigate(AddAttachment)
                        is AttachmentContentEvent.OnAttachmentOpen -> {
                            // open attachment
                        }
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

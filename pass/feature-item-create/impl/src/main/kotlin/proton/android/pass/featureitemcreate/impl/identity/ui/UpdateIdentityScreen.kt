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
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.ItemSavedLaunchedEffect
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.AddAttachment
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.AddExtraSection
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.CustomFieldOptions
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.ExtraSectionOptions
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.OpenAttachmentOptions
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.OpenDraftAttachmentOptions
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation.OpenExtraFieldBottomSheet
import proton.android.pass.featureitemcreate.impl.identity.navigation.CreateIdentityNavigation.SelectVault
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.navigation.UpdateIdentityNavigation
import proton.android.pass.featureitemcreate.impl.identity.navigation.bottomsheets.AddIdentityFieldType
import proton.android.pass.featureitemcreate.impl.identity.presentation.UpdateIdentityViewModel
import proton.android.pass.featureitemcreate.impl.login.PerformActionAfterKeyboardHide

@Composable
fun UpdateIdentityScreen(
    modifier: Modifier = Modifier,
    viewModel: UpdateIdentityViewModel = hiltViewModel(),
    onNavigate: (BaseIdentityNavigation) -> Unit
) {
    val context = LocalContext.current
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
            topBarActionName = stringResource(id = R.string.action_save),
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

                        AttachmentContentEvent.OnDeleteAllAttachments -> {
                            // delete all attachments
                        }
                        is AttachmentContentEvent.OnDraftAttachmentOpen ->
                            viewModel.openDraftAttachment(
                                context,
                                event.event.uri,
                                event.event.mimetype
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
        selectedShareId = state.getSelectedShareId().value(),
        onSuccess = { shareId, itemId, _ ->
            actionAfterKeyboardHide = {
                onNavigate(UpdateIdentityNavigation.IdentityUpdated(shareId, itemId))
            }
        }
    )
}

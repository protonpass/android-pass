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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

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
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.BaseCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.CreateCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.ClearDraft
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateCustomItemViewModel
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent.OnVaultSelected
import proton.android.pass.features.itemcreate.launchedeffects.InAppReviewTriggerLaunchedEffect
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Composable
fun CreateCustomItemScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateCustomItemViewModel = hiltViewModel(),
    onNavigate: (BaseCustomItemNavigation) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(selectVault) {
        selectVault ?: return@LaunchedEffect
        viewModel.processIntent(OnVaultSelected(selectVault))
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
            viewModel.processIntent(ClearDraft)
            actionAfterKeyboardHide = { onNavigate(BaseCustomItemNavigation.CloseScreen) }
        }
    }
    BackHandler(onBack = onExit)
    Box(modifier = modifier.fillMaxSize()) {
        CustomContent(
            itemFormState = viewModel.itemFormState,
            itemSharedProperties = state,
            topBarActionName = stringResource(id = R.string.title_create),
            onEvent = {
                when (it) {
                    ItemContentEvent.Up -> onExit()

                    is ItemContentEvent.OnTitleChange ->
                        viewModel.processIntent(BaseCustomItemCommonIntent.OnTitleChanged(it.value))

                    is ItemContentEvent.OnAddCustomField ->
                        actionAfterKeyboardHide = {
                            onNavigate(BaseCustomItemNavigation.AddCustomField(it.sectionIndex))
                        }

                    is ItemContentEvent.OnCustomFieldChange -> viewModel.processIntent(
                        BaseCustomItemCommonIntent.OnCustomFieldChanged(
                            index = it.index,
                            value = it.value,
                            sectionIndex = it.sectionIndex
                        )
                    )

                    is ItemContentEvent.OnCustomFieldOptions ->
                        actionAfterKeyboardHide = {
                            onNavigate(
                                BaseCustomItemNavigation.CustomFieldOptions(
                                    title = it.label,
                                    index = it.index,
                                    sectionIndex = it.sectionIndex
                                )
                            )
                        }

                    is ItemContentEvent.OnCustomFieldFocused -> viewModel.processIntent(
                        BaseCustomItemCommonIntent.OnCustomFieldFocusedChanged(
                            index = it.index,
                            value = it.isFocused,
                            sectionIndex = it.sectionIndex
                        )
                    )

                    ItemContentEvent.OnAddSection ->
                        actionAfterKeyboardHide =
                            { onNavigate(BaseCustomItemNavigation.AddSection) }

                    is ItemContentEvent.OnSectionOptions ->
                        actionAfterKeyboardHide = {
                            onNavigate(
                                BaseCustomItemNavigation.SectionOptions(
                                    title = it.label,
                                    index = it.index
                                )
                            )
                        }

                    is ItemContentEvent.OnAttachmentEvent -> when (val event = it.event) {
                        AttachmentContentEvent.OnAddAttachment ->
                            onNavigate(BaseCustomItemNavigation.AddAttachment)
                        is AttachmentContentEvent.OnAttachmentOpen ->
                            throw IllegalStateException("Cannot open attachment during create")

                        is AttachmentContentEvent.OnAttachmentOptions ->
                            onNavigate(
                                BaseCustomItemNavigation.OpenAttachmentOptions(
                                    shareId = event.shareId,
                                    itemId = event.itemId,
                                    attachmentId = event.attachmentId
                                )
                            )

                        AttachmentContentEvent.OnDeleteAllAttachments ->
                            onNavigate(
                                BaseCustomItemNavigation.DeleteAllAttachments(
                                    state.attachmentsState.allToUnlink
                                )
                            )

                        is AttachmentContentEvent.OnDraftAttachmentOpen ->
                            viewModel.processIntent(
                                BaseCustomItemCommonIntent.OnOpenDraftAttachment(
                                    contextHolder = context.toClassHolder(),
                                    uri = event.uri,
                                    mimetype = event.mimetype
                                )
                            )

                        is AttachmentContentEvent.OnDraftAttachmentOptions ->
                            onNavigate(BaseCustomItemNavigation.OpenDraftAttachmentOptions(event.uri))

                        is AttachmentContentEvent.OnDraftAttachmentRetry ->
                            viewModel.processIntent(
                                BaseCustomItemCommonIntent.OnRetryUploadAttachment(event.metadata)
                            )

                        AttachmentContentEvent.UpsellAttachments ->
                            onNavigate(BaseCustomItemNavigation.UpsellAttachments)
                    }

                    is ItemContentEvent.OnVaultSelect ->
                        actionAfterKeyboardHide = {
                            onNavigate(CreateCustomItemNavigation.SelectVault(it.shareId))
                        }

                    is ItemContentEvent.Submit ->
                        viewModel.processIntent(CreateSpecificIntent.SubmitCreate(it.shareId))

                    ItemContentEvent.ClearLastAddedFieldFocus ->
                        viewModel.processIntent(BaseCustomItemCommonIntent.ClearLastAddedFieldFocus)

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
                actionAfterKeyboardHide = { onNavigate(BaseCustomItemNavigation.CloseScreen) }
            }
        )
    }
    ItemSavedLaunchedEffect(
        isItemSaved = state.itemSavedState,
        selectedShareId = state.selectedVault.value()?.shareId,
        onSuccess = { _, _, model ->
            viewModel.processIntent(ClearDraft)
            actionAfterKeyboardHide =
                { onNavigate(CreateCustomItemNavigation.ItemCreated(model)) }
        }
    )
    InAppReviewTriggerLaunchedEffect(
        triggerCondition = state.itemSavedState is ItemSavedState.Success
    )
}

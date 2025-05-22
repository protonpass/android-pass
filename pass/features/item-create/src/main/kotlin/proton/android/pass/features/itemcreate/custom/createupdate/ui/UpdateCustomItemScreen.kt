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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.dialogs.ConfirmCloseDialog
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.ItemSavedLaunchedEffect
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEvent
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.BaseCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.BaseCustomItemNavigation.OpenWifiSecurityTypeSelector
import proton.android.pass.features.itemcreate.custom.createupdate.navigation.CreateCustomItemNavigation
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.ClearDraft
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnCustomFieldChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnPasswordChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnPasswordFocusedChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnPrivateKeyChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnPrivateKeyFocusedChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnPublicKeyChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnReceiveTotp
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnReceiveWifiSecurityType
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnSSIDChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnTitleChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnWifiSecurityTypeChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.UpdateCustomItemViewModel
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.UpdateSpecificIntent
import proton.android.pass.features.itemcreate.login.PerformActionAfterKeyboardHide

@Composable
fun UpdateCustomItemScreen(
    modifier: Modifier = Modifier,
    selectTotp: Triple<Option<String>, Option<Int>, Option<Int>>,
    selectWifiSecurityType: Option<WifiSecurityType>,
    viewModel: UpdateCustomItemViewModel = hiltViewModel(),
    onNavigate: (BaseCustomItemNavigation) -> Unit
) {
    val context = LocalContext.current
    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showDatePickerForField: Option<CustomFieldIdentifier> by remember { mutableStateOf(None) }

    val onExit = {
        if (state.hasUserEdited) {
            showConfirmDialog = !showConfirmDialog
        } else {
            viewModel.processIntent(ClearDraft)
            actionAfterKeyboardHide = { onNavigate(BaseCustomItemNavigation.CloseScreen) }
        }
    }
    LaunchedEffect(selectTotp) {
        val (totp, sectionIndex, index) = selectTotp
        if (totp is Some && index is Some) {
            viewModel.processIntent(OnReceiveTotp(totp.value, sectionIndex, index.value))
        }
    }

    LaunchedEffect(selectWifiSecurityType) {
        if (selectWifiSecurityType is Some) {
            viewModel.processIntent(OnReceiveWifiSecurityType(selectWifiSecurityType.value))
        }
    }

    BackHandler(onBack = onExit)
    Box(modifier = modifier.fillMaxSize()) {
        CustomContent(
            itemFormState = viewModel.itemFormState,
            itemSharedProperties = state,
            topBarActionName = stringResource(id = R.string.action_save),
            onEvent = {
                when (it) {
                    ItemContentEvent.Up -> onExit()
                    ItemContentEvent.OnUpgrade -> onNavigate(BaseCustomItemNavigation.Upgrade)

                    is ItemContentEvent.OnFieldValueChange -> when (it.field) {
                        FieldChange.Password ->
                            viewModel.processIntent(OnPasswordChanged(it.value as String))

                        FieldChange.PrivateKey ->
                            viewModel.processIntent(OnPrivateKeyChanged(it.value as String))

                        FieldChange.PublicKey ->
                            viewModel.processIntent(OnPublicKeyChanged(it.value as String))

                        FieldChange.SSID ->
                            viewModel.processIntent(OnSSIDChanged(it.value as String))

                        FieldChange.WifiSecurityType ->
                            viewModel.processIntent(OnWifiSecurityTypeChanged(it.value as Int))

                        FieldChange.Title ->
                            viewModel.processIntent(OnTitleChanged(it.value as String))
                    }

                    is ItemContentEvent.OnFieldFocusChange -> when (it.field) {
                        FieldChange.Password ->
                            viewModel.processIntent(OnPasswordFocusedChanged(it.isFocused))

                        FieldChange.PrivateKey ->
                            viewModel.processIntent(OnPrivateKeyFocusedChanged(it.isFocused))

                        else -> throw IllegalStateException("Ignore focus change for ${it.field}")
                    }
                    is ItemContentEvent.OnCustomFieldEvent -> when (val cevent = it.event) {
                        is CustomFieldEvent.FocusRequested -> viewModel.processIntent(
                            BaseCustomItemCommonIntent.OnCustomFieldFocusedChanged(
                                field = cevent.field,
                                isFocused = cevent.isFocused
                            )
                        )
                        is CustomFieldEvent.OnAddField -> actionAfterKeyboardHide = {
                            onNavigate(BaseCustomItemNavigation.AddCustomField(cevent.sectionIndex))
                        }
                        is CustomFieldEvent.OnFieldClick -> when (cevent.field.type) {
                            CustomFieldType.Date -> {
                                showDatePickerForField = Some(cevent.field)
                            }
                            else -> throw IllegalStateException("Unhandled action")
                        }
                        is CustomFieldEvent.OnFieldOptions -> actionAfterKeyboardHide = {
                            onNavigate(
                                BaseCustomItemNavigation.CustomFieldOptions(
                                    title = cevent.label,
                                    index = cevent.field.index,
                                    sectionIndex = cevent.field.sectionIndex
                                )
                            )
                        }
                        is CustomFieldEvent.OnValueChange -> viewModel.processIntent(
                            OnCustomFieldChanged(cevent.field, cevent.value)
                        )
                        CustomFieldEvent.Upgrade -> onNavigate(BaseCustomItemNavigation.Upgrade)
                    }

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
                            viewModel.processIntent(
                                UpdateSpecificIntent.OnOpenAttachment(
                                    contextHolder = context.toClassHolder(),
                                    attachment = event.attachment
                                )
                            )

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
                        viewModel.processIntent(UpdateSpecificIntent.SubmitUpdate)

                    ItemContentEvent.DismissAttachmentBanner ->
                        viewModel.processIntent(BaseCustomItemCommonIntent.DismissFileAttachmentsBanner)

                    is ItemContentEvent.OnOpenTOTPScanner ->
                        onNavigate(
                            BaseCustomItemNavigation.OpenTOTPScanner(
                                it.sectionIndex,
                                it.index
                            )
                        )

                    ItemContentEvent.OnPasteTOTPSecret ->
                        viewModel.processIntent(BaseCustomItemCommonIntent.PasteTOTPSecret)

                    is ItemContentEvent.OnOpenWifiSecurityType ->
                        onNavigate(OpenWifiSecurityTypeSelector(it.wifiSecurityType))
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
                viewModel.processIntent(ClearDraft)
                actionAfterKeyboardHide = { onNavigate(BaseCustomItemNavigation.CloseScreen) }
            }
        )
        showDatePickerForField.value()?.let { fieldIdentifier ->
            val selectedDate = viewModel.itemFormState
                .findCustomField(fieldIdentifier) as UICustomFieldContent.Date
            DatePickerModal(
                selectedDate = selectedDate.value,
                onDateSelected = {
                    viewModel.processIntent(OnCustomFieldChanged(fieldIdentifier, it.toString()))
                },
                onDismiss = { showDatePickerForField = None }
            )
        }
    }
    ItemSavedLaunchedEffect(
        isItemSaved = state.itemSavedState,
        selectedShareId = state.selectedShareId.value(),
        onSuccess = { _, _, model ->
            viewModel.processIntent(ClearDraft)
            actionAfterKeyboardHide =
                { onNavigate(CreateCustomItemNavigation.ItemCreated(model)) }
        }
    )
}

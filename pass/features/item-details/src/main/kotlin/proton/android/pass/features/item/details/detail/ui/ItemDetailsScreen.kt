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

package proton.android.pass.features.item.details.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.ItemState
import proton.android.pass.features.item.details.detail.presentation.ItemDetailsEvent
import proton.android.pass.features.item.details.detail.presentation.ItemDetailsViewModel
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination

@Composable
fun ItemDetailsScreen(
    onNavigated: (ItemDetailsNavDestination) -> Unit,
    viewModel: ItemDetailsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.event) {
        when (state.event) {
            ItemDetailsEvent.Idle -> {
            }

            ItemDetailsEvent.OnItemNotFound ->
                ItemDetailsNavDestination.Home
                    .also(onNavigated)
        }

        onConsumeEvent(state.event)
    }

    ItemDetailsContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemDetailsUiEvent.OnNavigateBack ->
                    ItemDetailsNavDestination.Back
                        .also(onNavigated)

                is ItemDetailsUiEvent.OnEditClicked -> ItemDetailsNavDestination.EditItem(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId,
                    itemCategory = uiEvent.itemCategory
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnDisabledEditClicked -> uiEvent.reason?.let { reason ->
                    ItemDetailsNavDestination.ItemActionForbidden(reason = reason)
                        .also(onNavigated)
                }

                is ItemDetailsUiEvent.OnPasskeyClicked -> ItemDetailsNavDestination.PasskeyDetails(
                    passkeyContent = uiEvent.passkeyContent
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnFieldClicked -> onItemFieldClicked(
                    text = uiEvent.text,
                    plainFieldType = uiEvent.field
                )

                is ItemDetailsUiEvent.OnHiddenFieldClicked -> onItemHiddenFieldClicked(
                    hiddenState = uiEvent.state,
                    hiddenFieldType = uiEvent.field
                )

                is ItemDetailsUiEvent.OnHiddenFieldToggled -> onToggleItemHiddenField(
                    isVisible = uiEvent.isVisible,
                    hiddenState = uiEvent.state,
                    hiddenFieldType = uiEvent.fieldType,
                    hiddenFieldSection = uiEvent.fieldSection
                )

                is ItemDetailsUiEvent.OnLinkClicked -> BrowserUtils.openWebsite(
                    context = context,
                    website = uiEvent.link
                )

                is ItemDetailsUiEvent.OnViewItemHistoryClicked -> ItemDetailsNavDestination.ItemHistory(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnShareItemClicked -> ItemDetailsNavDestination.ItemSharing(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnDisabledShareItemClicked -> uiEvent.reason?.let { reason ->
                    ItemDetailsNavDestination.ItemActionForbidden(reason = reason)
                        .also(onNavigated)
                }

                is ItemDetailsUiEvent.OnSharedVaultClicked -> ItemDetailsNavDestination.ManageSharedVault(
                    sharedVaultId = uiEvent.sharedVaultId,
                    itemCategory = uiEvent.itemCategory
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnMenuClicked -> when (uiEvent.itemState) {
                    ItemState.Active -> ItemDetailsNavDestination.ItemOptionsMenu(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    )

                    ItemState.Trashed -> ItemDetailsNavDestination.ItemTrashMenu(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    )
                }.also(onNavigated)

                is ItemDetailsUiEvent.OnAttachmentEvent ->
                    when (uiEvent.attachmentContentEvent) {
                        is AttachmentContentEvent.OnAttachmentOpen ->
                            viewModel.onAttachmentOpen(context, uiEvent.attachmentContentEvent.attachment)
                        is AttachmentContentEvent.OnAttachmentOptions,
                        AttachmentContentEvent.OnAddAttachment,
                        AttachmentContentEvent.OnDeleteAllAttachments,
                        is AttachmentContentEvent.OnDraftAttachmentOpen,
                        is AttachmentContentEvent.OnDraftAttachmentOptions ->
                            throw IllegalStateException("Action not allowed: $uiEvent")
                    }
            }
        }
    )
}

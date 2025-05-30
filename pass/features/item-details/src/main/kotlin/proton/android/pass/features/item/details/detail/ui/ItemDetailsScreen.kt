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
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.commonuimodels.api.items.AliasDetailEvent
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.domain.ItemState
import proton.android.pass.features.item.details.detail.presentation.ItemDetailsEvent
import proton.android.pass.features.item.details.detail.presentation.ItemDetailsState
import proton.android.pass.features.item.details.detail.presentation.ItemDetailsViewModel
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ContactSection
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.EditItem
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ItemActionForbidden
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ItemHistory
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ItemOptionsMenu
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ItemSharing
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ItemTrashMenu
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ManageSharedVault
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.OnCreateLoginFromAlias
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.OpenAttachmentOptions
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.PasskeyDetails
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.ViewReusedPasswords
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination.WifiNetworkQRClick

@Composable
fun ItemDetailsScreen(
    onNavigated: (ItemDetailsNavDestination) -> Unit,
    viewModel: ItemDetailsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.event) {
        when (state.event) {
            ItemDetailsEvent.Idle -> Unit

            ItemDetailsEvent.OnItemNotFound ->
                ItemDetailsNavDestination.Home
                    .also(onNavigated)
        }

        onConsumeEvent(state.event)
    }

    when (val success = state) {
        is ItemDetailsState.Success -> LaunchedEffect(success.itemDetailState.detailEvent) {
            when (val event = success.itemDetailState.detailEvent) {
                is AliasDetailEvent.ContactSection ->
                    ContactSection(event.shareId, event.itemId)
                        .also(onNavigated)
                is AliasDetailEvent.CreateLoginFromAlias ->
                    OnCreateLoginFromAlias(event.alias, event.shareId)
                        .also(onNavigated)
                DetailEvent.Idle -> {}
            }
            onConsumeInternalEvent(success.itemDetailState.detailEvent)
        }

        else -> {}
    }

    ItemDetailsContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemDetailsUiEvent.OnUpgrade ->
                    ItemDetailsNavDestination.Upgrade
                        .also(onNavigated)

                ItemDetailsUiEvent.OnNavigateBack ->
                    ItemDetailsNavDestination.CloseScreen
                        .also(onNavigated)

                is ItemDetailsUiEvent.OnEditClicked -> EditItem(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId,
                    itemCategory = uiEvent.itemCategory
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnDisabledEditClicked -> uiEvent.reason?.let { reason ->
                    ItemActionForbidden(reason = reason)
                        .also(onNavigated)
                }

                is ItemDetailsUiEvent.OnPasskeyClicked -> PasskeyDetails(
                    passkeyContent = uiEvent.passkeyContent
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnFieldClicked -> onItemFieldClicked(
                    fieldType = uiEvent.field
                )

                is ItemDetailsUiEvent.OnHiddenFieldToggled -> onToggleItemHiddenField(
                    isVisible = uiEvent.isVisible,
                    hiddenFieldType = uiEvent.fieldType,
                    hiddenFieldSection = uiEvent.fieldSection
                )

                is ItemDetailsUiEvent.OnLinkClicked -> BrowserUtils.openWebsite(
                    context = context,
                    website = uiEvent.link
                )

                is ItemDetailsUiEvent.OnViewItemHistoryClicked -> ItemHistory(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnShareItemClicked -> ItemSharing(
                    shareId = uiEvent.shareId,
                    itemId = uiEvent.itemId
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnDisabledShareItemClicked -> uiEvent.reason?.let { reason ->
                    ItemActionForbidden(reason = reason)
                        .also(onNavigated)
                }

                is ItemDetailsUiEvent.OnSharedVaultClicked -> ManageSharedVault(
                    sharedVaultId = uiEvent.sharedVaultId,
                    itemCategory = uiEvent.itemCategory
                ).also(onNavigated)

                is ItemDetailsUiEvent.OnMenuClicked -> when (uiEvent.itemState) {
                    ItemState.Active -> ItemOptionsMenu(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    )

                    ItemState.Trashed -> ItemTrashMenu(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    )
                }.also(onNavigated)

                is ItemDetailsUiEvent.OnAttachmentEvent ->
                    when (val event = uiEvent.attachmentContentEvent) {
                        is AttachmentContentEvent.OnAttachmentOpen ->
                            viewModel.onAttachmentOpen(
                                context = context.toClassHolder(),
                                attachment = event.attachment
                            )

                        is AttachmentContentEvent.OnAttachmentOptions -> onNavigated(
                            OpenAttachmentOptions(
                                shareId = event.shareId,
                                itemId = event.itemId,
                                attachmentId = event.attachmentId
                            )
                        )

                        AttachmentContentEvent.OnAddAttachment,
                        AttachmentContentEvent.UpsellAttachments,
                        AttachmentContentEvent.OnDeleteAllAttachments,
                        is AttachmentContentEvent.OnDraftAttachmentOpen,
                        is AttachmentContentEvent.OnDraftAttachmentRetry,
                        is AttachmentContentEvent.OnDraftAttachmentOptions ->
                            throw IllegalStateException("Action not allowed: $uiEvent")
                    }

                is ItemDetailsUiEvent.OnWifiNetworkQRClick ->
                    WifiNetworkQRClick(uiEvent.rawSvg)
                        .also(onNavigated)

                is ItemDetailsUiEvent.OnViewReusedPasswords ->
                    ViewReusedPasswords(uiEvent.shareId, uiEvent.itemId)
                        .also(onNavigated)
            }
        }
    )
}

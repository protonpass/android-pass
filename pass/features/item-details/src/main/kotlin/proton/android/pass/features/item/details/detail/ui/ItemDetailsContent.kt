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
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsContent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.features.item.details.detail.presentation.ItemDetailsState
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnAttachmentEvent
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnFieldClicked
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnHiddenFieldClicked
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnHiddenFieldToggled
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnLinkClicked
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnPasskeyClicked
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnSharedVaultClicked
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnViewItemHistoryClicked
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.OnWifiNetworkQRClick
import proton.android.pass.features.item.details.detail.ui.ItemDetailsUiEvent.ViewReusedPasswords

@Composable
internal fun ItemDetailsContent(
    modifier: Modifier = Modifier,
    onEvent: (ItemDetailsUiEvent) -> Unit,
    state: ItemDetailsState
) = with(state) {
    when (this) {
        ItemDetailsState.Error -> Unit

        ItemDetailsState.Loading -> {
            PassFullScreenLoading()
        }

        is ItemDetailsState.Success -> {
            val itemColors = passItemColors(itemCategory = itemDetailState.itemCategory)

            PassItemDetailsContent(
                modifier = modifier,
                itemDetailState = itemDetailState,
                itemColors = itemColors,
                shouldDisplayItemHistorySection = true,
                shouldDisplayItemHistoryButton = canViewItemHistory,
                shouldDisplayFileAttachments = isFileAttachmentsEnabled,
                shouldDisplayCustomItems = isCustomItemEnabled,
                topBar = {
                    ItemDetailsTopBar(
                        isLoading = false,
                        itemCategory = itemDetailState.itemCategory,
                        shareSharedCount = shareSharedCount,
                        onUpClick = {
                            ItemDetailsUiEvent.OnNavigateBack
                                .also(onEvent)
                        },
                        isEditEnabled = isEditEnabled,
                        onEditClick = {
                            if (isEditEnabled) {
                                ItemDetailsUiEvent.OnEditClicked(
                                    shareId = shareId,
                                    itemId = itemId,
                                    itemCategory = itemDetailState.itemCategory
                                )
                            } else {
                                ItemDetailsUiEvent.OnDisabledEditClicked(reason = cannotEditReason)
                            }.also(onEvent)
                        },
                        areOptionsEnabled = areOptionsEnabled,
                        onOptionsClick = {
                            ItemDetailsUiEvent.OnMenuClicked(
                                shareId = shareId,
                                itemId = itemId,
                                itemState = itemDetailState.itemState
                            ).also(onEvent)
                        },
                        isShareEnabled = isShareEnabled,
                        onShareClick = {
                            if (isShareEnabled) {
                                ItemDetailsUiEvent.OnShareItemClicked(
                                    shareId = shareId,
                                    itemId = itemId
                                )
                            } else {
                                ItemDetailsUiEvent.OnDisabledShareItemClicked(reason = cannotShareReason)
                            }.also(onEvent)
                        }
                    )
                },
                onEvent = { uiEvent ->
                    when (uiEvent) {
                        is PassItemDetailsUiEvent.OnHiddenFieldClick -> OnHiddenFieldClicked(
                            state = uiEvent.state,
                            field = uiEvent.field
                        )

                        is PassItemDetailsUiEvent.OnHiddenFieldToggle -> OnHiddenFieldToggled(
                            isVisible = uiEvent.isVisible,
                            state = uiEvent.hiddenState,
                            fieldType = uiEvent.fieldType,
                            fieldSection = uiEvent.fieldSection
                        )

                        is PassItemDetailsUiEvent.OnLinkClick -> OnLinkClicked(
                            link = uiEvent.link
                        )

                        is PassItemDetailsUiEvent.OnPasskeyClick -> OnPasskeyClicked(
                            passkeyContent = uiEvent.passkey
                        )

                        is PassItemDetailsUiEvent.OnSectionClick -> OnFieldClicked(
                            text = uiEvent.section,
                            field = uiEvent.field
                        )

                        PassItemDetailsUiEvent.OnViewItemHistoryClick -> OnViewItemHistoryClicked(
                            shareId = shareId,
                            itemId = itemId
                        )

                        is PassItemDetailsUiEvent.OnSharedVaultClick -> OnSharedVaultClicked(
                            sharedVaultId = uiEvent.sharedVaultId,
                            itemCategory = itemDetailState.itemCategory
                        )

                        is PassItemDetailsUiEvent.OnAttachmentEvent -> OnAttachmentEvent(
                            attachmentContentEvent = uiEvent.attachmentContentEvent
                        )

                        is PassItemDetailsUiEvent.OnWifiNetworkQRClick ->
                            OnWifiNetworkQRClick(uiEvent.rawSvg)

                        PassItemDetailsUiEvent.OnShowReusedPasswords ->
                            ViewReusedPasswords(
                                shareId = shareId,
                                itemId = itemId
                            )
                    }.also(onEvent)
                }
            )
        }
    }
}

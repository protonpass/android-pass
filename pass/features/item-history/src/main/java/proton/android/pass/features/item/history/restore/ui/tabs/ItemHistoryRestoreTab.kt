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

package proton.android.pass.features.item.history.restore.ui.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsContent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent.OnFieldClick
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent.OnHiddenFieldToggle
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent.OnLinkClick
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent.OnPasskeyClick
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent.OnUpgrade
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent.OnWifiNetworkQRClick
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreSelection
import proton.android.pass.features.item.history.restore.ui.ItemHistoryRestoreTopBar

@Composable
internal fun ItemHistoryRestoreTab(
    modifier: Modifier = Modifier,
    itemDetailState: ItemDetailState,
    itemColors: PassItemColors,
    isCustomItemEnabled: Boolean,
    onEvent: (ItemHistoryRestoreUiEvent) -> Unit,
    selection: ItemHistoryRestoreSelection
) {
    PassItemDetailsContent(
        modifier = modifier,
        topBar = {
            ItemHistoryRestoreTopBar(
                colors = itemColors,
                onUpClick = { onEvent(ItemHistoryRestoreUiEvent.OnBackClick) },
                onRestoreClick = { onEvent(ItemHistoryRestoreUiEvent.OnRestoreClick) }
            )
        },
        itemDetailState = itemDetailState,
        itemColors = itemColors,
        onEvent = { uiEvent ->
            when (uiEvent) {
                is PassItemDetailsUiEvent.OnUpgrade -> OnUpgrade

                is PassItemDetailsUiEvent.OnFieldClick -> OnFieldClick(
                    field = uiEvent.field
                ).also(onEvent)

                is PassItemDetailsUiEvent.OnHiddenFieldToggle -> OnHiddenFieldToggle(
                    selection = selection,
                    isVisible = uiEvent.isVisible,
                    hiddenState = uiEvent.hiddenState,
                    fieldType = uiEvent.fieldType,
                    fieldSection = uiEvent.fieldSection
                ).also(onEvent)

                is PassItemDetailsUiEvent.OnLinkClick -> OnLinkClick(
                    linkUrl = uiEvent.link
                ).also(onEvent)

                is PassItemDetailsUiEvent.OnPasskeyClick -> OnPasskeyClick(
                    passkey = uiEvent.passkey
                ).also(onEvent)

                PassItemDetailsUiEvent.OnViewItemHistoryClick -> {
                    // We do nothing since item history widget shouldn't appear on restore screen
                }

                is PassItemDetailsUiEvent.OnSharedVaultClick -> {
                    // We do nothing since we don't allow shared vault management from restore screen
                }

                is PassItemDetailsUiEvent.OnAttachmentEvent -> when (uiEvent.attachmentContentEvent) {
                    AttachmentContentEvent.OnAddAttachment,
                    is AttachmentContentEvent.OnAttachmentOpen,
                    is AttachmentContentEvent.OnAttachmentOptions,
                    AttachmentContentEvent.OnDeleteAllAttachments,
                    AttachmentContentEvent.UpsellAttachments,
                    is AttachmentContentEvent.OnDraftAttachmentOpen,
                    is AttachmentContentEvent.OnDraftAttachmentOptions,
                    is AttachmentContentEvent.OnDraftAttachmentRetry -> {
                        // We do nothing since we don't allow attachment management from restore screen
                    }
                }
                is PassItemDetailsUiEvent.OnWifiNetworkQRClick ->
                    OnWifiNetworkQRClick(rawSvg = uiEvent.rawSvg)
                        .also(onEvent)

                PassItemDetailsUiEvent.OnShowReusedPasswords -> {
                    // We do nothing since the widget should not show from restore screen
                }
            }
        },
        shouldDisplayItemHistorySection = false,
        shouldDisplayItemHistoryButton = false,
        shouldDisplayCustomItems = isCustomItemEnabled,
        extraBottomSpacing = Spacing.extraLarge
    )
}

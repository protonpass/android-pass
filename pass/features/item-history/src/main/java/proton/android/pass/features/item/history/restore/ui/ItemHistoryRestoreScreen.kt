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

package proton.android.pass.features.item.history.restore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreViewModel

@Composable
fun ItemHistoryRestoreScreen(
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    viewModel: ItemHistoryRestoreViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ItemHistoryRestoreContent(
        onNavigated = onNavigated,
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemHistoryRestoreUiEvent.OnUpgrade -> {
                    onNavigated(ItemHistoryNavDestination.Upgrade)
                }

                ItemHistoryRestoreUiEvent.OnBackClick -> {
                    ItemHistoryNavDestination.CloseScreen
                        .also(onNavigated)
                }

                is ItemHistoryRestoreUiEvent.OnEventConsumed -> {
                    onEventConsumed(uiEvent.event)
                }

                is ItemHistoryRestoreUiEvent.OnHiddenFieldToggle -> {
                    onToggleItemHiddenField(
                        selection = uiEvent.selection,
                        isVisible = uiEvent.isVisible,
                        hiddenFieldType = uiEvent.fieldType,
                        itemSection = uiEvent.fieldSection
                    )
                }

                is ItemHistoryRestoreUiEvent.OnPasskeyClick -> {
                    onNavigated(ItemHistoryNavDestination.PasskeyDetail(uiEvent.passkey))
                }

                ItemHistoryRestoreUiEvent.OnRestoreCancelClick -> {
                    onRestoreItemCanceled()
                }

                ItemHistoryRestoreUiEvent.OnRestoreClick -> {
                    onRestoreItem()
                }

                is ItemHistoryRestoreUiEvent.OnRestoreConfirmClick -> {
                    onRestoreItemConfirmed(
                        itemContents = uiEvent.contents,
                        attachmentsToRestore = uiEvent.attachmentsToRestore,
                        attachmentsToDelete = uiEvent.attachmentsToDelete
                    )
                }

                is ItemHistoryRestoreUiEvent.OnFieldClick -> {
                    onItemFieldClicked(uiEvent.field)
                }

                is ItemHistoryRestoreUiEvent.OnLinkClick -> {
                    BrowserUtils.openWebsite(
                        context = context,
                        website = uiEvent.linkUrl
                    )
                }
                is ItemHistoryRestoreUiEvent.OnWifiNetworkQRClick ->
                    onNavigated(ItemHistoryNavDestination.WifiNetworkQR(uiEvent.rawSvg))
            }
        }
    )
}

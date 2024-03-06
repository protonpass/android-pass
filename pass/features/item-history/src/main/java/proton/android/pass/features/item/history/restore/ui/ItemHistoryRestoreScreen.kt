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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreViewModel

@Composable
fun ItemHistoryRestoreScreen(
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    viewModel: ItemHistoryRestoreViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    ItemHistoryRestoreContent(
        onNavigated = onNavigated,
        state = state,
        onEvent = {
            when (it) {
                is ItemHistoryRestoreUiEvent.OnEventConsumed -> onEventConsumed(it.event)
                is ItemHistoryRestoreUiEvent.OnHiddenSectionClick -> {
                    onItemHiddenFieldClicked(it.state, it.field)
                }
                is ItemHistoryRestoreUiEvent.OnHiddenSectionToggle -> {
                    onItemHiddenFieldToggled(it.state, it.hiddenState, it.field)
                }
                is ItemHistoryRestoreUiEvent.OnPasskeyClick -> {
                    onNavigated(ItemHistoryNavDestination.PasskeyDetail(it.passkey))
                }
                ItemHistoryRestoreUiEvent.OnRestoreCancelClick -> {
                    onRestoreItemCanceled()
                }
                ItemHistoryRestoreUiEvent.OnRestoreClick -> {
                    onRestoreItem()
                }
                is ItemHistoryRestoreUiEvent.OnRestoreConfirmClick -> {
                    onRestoreItemConfirmed(it.contents)
                }
                is ItemHistoryRestoreUiEvent.OnSectionClick -> {
                    onItemFieldClicked(it.section, it.field)
                }
            }
        }
    )
}

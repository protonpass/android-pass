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

package proton.android.pass.features.item.trash.trashmenu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.item.trash.shared.navigation.ItemTrashNavDestination
import proton.android.pass.features.item.trash.trashmenu.presentation.ItemTrashMenuEvent
import proton.android.pass.features.item.trash.trashmenu.presentation.ItemTrashMenuViewModel

@Composable
fun ItemTrashMenuBottomSheet(
    onNavigated: (ItemTrashNavDestination) -> Unit,
    viewModel: ItemTrashMenuViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.event) {
        when (val event = state.event) {
            ItemTrashMenuEvent.Idle -> Unit

            is ItemTrashMenuEvent.OnLeaveItem -> ItemTrashNavDestination.LeaveItem(
                shareId = event.shareId
            ).also(onNavigated)

            is ItemTrashMenuEvent.OnDeleteItem -> ItemTrashNavDestination.DeleteItem(
                shareId = event.shareId,
                itemId = event.itemId
            ).also(onNavigated)

            ItemTrashMenuEvent.OnItemRestored ->
                ItemTrashNavDestination.Home
                    .also(onNavigated)

            ItemTrashMenuEvent.OnItemRestoreError ->
                ItemTrashNavDestination.DismissBottomSheet
                    .also(onNavigated)
        }

        onConsumeEvent(state.event)
    }

    ItemTrashMenuContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemTrashMenuUiEvent.OnLeaveItemClicked -> onLeaveItem()
                ItemTrashMenuUiEvent.OnDeleteItemPermanentlyClicked -> onDeleteItem()
                ItemTrashMenuUiEvent.OnRestoreItemClicked -> onRestoreItem()
            }
        }
    )
}

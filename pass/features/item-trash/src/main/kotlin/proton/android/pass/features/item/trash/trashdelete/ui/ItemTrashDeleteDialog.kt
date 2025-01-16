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

package proton.android.pass.features.item.trash.trashdelete.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.item.trash.shared.navigation.ItemTrashNavDestination
import proton.android.pass.features.item.trash.trashdelete.presentation.ItemTrashDeleteEvent
import proton.android.pass.features.item.trash.trashdelete.presentation.ItemTrashDeleteViewModel

@Composable
fun ItemTrashDeleteDialog(
    onNavigated: (ItemTrashNavDestination) -> Unit,
    viewModel: ItemTrashDeleteViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.event) {
        when (state.event) {
            ItemTrashDeleteEvent.Idle -> {}

            ItemTrashDeleteEvent.OnItemDeleteError ->
                ItemTrashNavDestination.DismissBottomSheet
                    .also(onNavigated)

            ItemTrashDeleteEvent.OnItemDeleted ->
                ItemTrashNavDestination.Home
                    .also(onNavigated)
        }

        onConsumeEvent(state.event)
    }

    ItemTrashDeleteContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemTrashDeleteUiEvent.OnCancelButtonClicked ->
                    ItemTrashNavDestination.CloseScreen
                        .also(onNavigated)

                ItemTrashDeleteUiEvent.OnConfirmButtonClicked -> onDeleteItem()
            }
        }
    )
}

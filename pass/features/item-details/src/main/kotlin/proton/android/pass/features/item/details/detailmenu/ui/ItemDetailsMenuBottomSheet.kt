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

package proton.android.pass.features.item.details.detailmenu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.item.details.detailmenu.presentation.ItemDetailsMenuEvent
import proton.android.pass.features.item.details.detailmenu.presentation.ItemDetailsMenuViewModel
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination

@Composable
fun ItemDetailsMenuBottomSheet(
    onNavigated: (ItemDetailsNavDestination) -> Unit,
    viewModel: ItemDetailsMenuViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.event) {
        when (state.event) {
            ItemDetailsMenuEvent.Idle -> {}

            ItemDetailsMenuEvent.OnItemNotFound,
            ItemDetailsMenuEvent.OnItemTrashed -> ItemDetailsNavDestination.Home
                .also(onNavigated)

            ItemDetailsMenuEvent.OnItemMigrated -> ItemDetailsNavDestination.ItemMigration
                .also(onNavigated)

            ItemDetailsMenuEvent.OnItemMigrationError,
            ItemDetailsMenuEvent.OnItemPinned,
            ItemDetailsMenuEvent.OnItemPinningError,
            ItemDetailsMenuEvent.OnItemUnpinned,
            ItemDetailsMenuEvent.OnItemUnpinningError,
            ItemDetailsMenuEvent.OnItemTrashingError -> ItemDetailsNavDestination.DismissBottomSheet
                .also(onNavigated)
        }

        onConsumeEvent(state.event)
    }

    ItemDetailsMenuContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemDetailsMenuUiEvent.OnMigrateItemClicked -> onMigrateItem()
                ItemDetailsMenuUiEvent.OnPinItemClicked -> onPinItem()
                ItemDetailsMenuUiEvent.OnUnpinItemClicked -> onUnpinItem()
                ItemDetailsMenuUiEvent.OnTrashItemClicked -> onTrashItem()
            }
        }
    )
}

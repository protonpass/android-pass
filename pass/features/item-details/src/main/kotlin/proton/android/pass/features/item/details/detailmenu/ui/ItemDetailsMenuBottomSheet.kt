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
        when (val event = state.event) {
            ItemDetailsMenuEvent.Idle -> Unit

            ItemDetailsMenuEvent.OnItemNotFound,
            ItemDetailsMenuEvent.OnItemTrashed -> {
                onNavigated(ItemDetailsNavDestination.Home)
            }

            ItemDetailsMenuEvent.OnItemMigrated -> {
                onNavigated(ItemDetailsNavDestination.ItemMigration)
            }

            ItemDetailsMenuEvent.OnItemSharedMigrated -> {
                onNavigated(ItemDetailsNavDestination.ItemSharedMigration)
            }

            is ItemDetailsMenuEvent.OnItemLeaved -> {
                ItemDetailsNavDestination.LeaveItemShare(
                    shareId = event.shareId
                ).also(onNavigated)
            }

            is ItemDetailsMenuEvent.OnItemClone -> {
                val category = state.itemOption.value()?.itemType?.category
                if (category != null) {
                    ItemDetailsNavDestination.DuplicateItem(
                        shareId = event.shareId,
                        itemId = event.itemId,
                        category = category
                    ).also(onNavigated)
                } else {
                    onNavigated(ItemDetailsNavDestination.CloseScreen)
                }
            }

            ItemDetailsMenuEvent.OnItemNoteCopied,
            ItemDetailsMenuEvent.OnItemMigrationError,
            ItemDetailsMenuEvent.OnItemMonitorExcluded,
            ItemDetailsMenuEvent.OnItemMonitorExcludedError,
            ItemDetailsMenuEvent.OnItemMonitorIncluded,
            ItemDetailsMenuEvent.OnItemMonitorIncludedError,
            ItemDetailsMenuEvent.OnItemPinned,
            ItemDetailsMenuEvent.OnItemPinningError,
            ItemDetailsMenuEvent.OnItemUnpinned,
            ItemDetailsMenuEvent.OnItemUnpinningError,
            ItemDetailsMenuEvent.OnItemTrashingError -> {
                onNavigated(ItemDetailsNavDestination.DismissBottomSheet)
            }
        }

        onConsumeEvent(state.event)
    }

    ItemDetailsMenuContent(
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                ItemDetailsMenuUiEvent.OnCopyItemNoteClicked -> onCopyItemNote()
                ItemDetailsMenuUiEvent.OnMigrateItemClicked -> onMigrateItem()
                ItemDetailsMenuUiEvent.OnPinItemClicked -> onPinItem()
                ItemDetailsMenuUiEvent.OnUnpinItemClicked -> onUnpinItem()
                ItemDetailsMenuUiEvent.OnExcludeItemMonitorClicked -> onExcludeItemFromMonitoring()
                ItemDetailsMenuUiEvent.OnIncludeItemMonitorClicked -> onIncludeItemInMonitoring()
                ItemDetailsMenuUiEvent.OnCloneItemClicked -> onCloneItem()
                ItemDetailsMenuUiEvent.OnTrashItemClicked -> onTrashItem()
                ItemDetailsMenuUiEvent.OnLeaveItemClicked -> onLeaveItem()
            }
        }
    )
}

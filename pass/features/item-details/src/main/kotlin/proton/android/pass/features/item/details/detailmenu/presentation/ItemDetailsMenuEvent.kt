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

package proton.android.pass.features.item.details.detailmenu.presentation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

internal sealed interface ItemDetailsMenuEvent {

    data object Idle : ItemDetailsMenuEvent

    data object OnItemNotFound : ItemDetailsMenuEvent

    data object OnItemNoteCopied : ItemDetailsMenuEvent

    data object OnItemMigrated : ItemDetailsMenuEvent

    data object OnItemMigrationError : ItemDetailsMenuEvent

    data object OnItemMonitorExcluded : ItemDetailsMenuEvent

    data object OnItemMonitorExcludedError : ItemDetailsMenuEvent

    data object OnItemMonitorIncluded : ItemDetailsMenuEvent

    data object OnItemMonitorIncludedError : ItemDetailsMenuEvent

    data object OnItemPinned : ItemDetailsMenuEvent

    data object OnItemPinningError : ItemDetailsMenuEvent

    data object OnItemUnpinned : ItemDetailsMenuEvent

    data object OnItemUnpinningError : ItemDetailsMenuEvent

    data object OnItemTrashed : ItemDetailsMenuEvent

    data object OnItemTrashingError : ItemDetailsMenuEvent

    @JvmInline
    value class OnItemLeaved(val shareId: ShareId) : ItemDetailsMenuEvent

    data class OnItemSharedTrashed(val shareId: ShareId, val itemId: ItemId) : ItemDetailsMenuEvent

    data object OnItemSharedMigrated : ItemDetailsMenuEvent

}

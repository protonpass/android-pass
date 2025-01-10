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

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.Item
import proton.android.pass.domain.Share
import proton.android.pass.domain.items.ItemCategory

@Stable
internal data class ItemDetailsMenuState(
    internal val action: BottomSheetItemAction,
    internal val event: ItemDetailsMenuEvent,
    private val itemOption: Option<Item>,
    private val itemActionsOption: Option<ItemActions>,
    private val shareOption: Option<Share>
) {

    private val itemCategory = when (itemOption) {
        None -> ItemCategory.Unknown
        is Some -> itemOption.value.itemType.category
    }

    private val isItemShare = when (shareOption) {
        None -> false
        is Some -> shareOption.value is Share.Item
    }

    internal val isItemPinned: Boolean = when (itemOption) {
        None -> false
        is Some -> itemOption.value.isPinned
    }

    internal val canBeMonitored: Boolean = itemCategory == ItemCategory.Login

    internal val canCopyItemNote: Boolean = itemCategory == ItemCategory.Note

    internal val canLeaveItem: Boolean = isItemShare

    internal val canMigrateItem: Boolean = when {
        isItemShare -> false
        else -> when (itemActionsOption) {
            None -> false
            is Some -> itemActionsOption.value.canMoveToOtherVault.value()
        }
    }

    internal val canTrashItem: Boolean = when {
        isItemShare -> false
        else -> when (itemActionsOption) {
            None -> false
            is Some -> itemActionsOption.value.canMoveToTrash
        }
    }

    internal val isItemExcludedFromMonitoring: Boolean by lazy {
        when (itemOption) {
            None -> false
            is Some -> itemOption.value.hasSkippedHealthCheck
        }
    }

    internal val itemEncryptedNote: String by lazy {
        when (itemOption) {
            None -> ""
            is Some -> itemOption.value.note
        }
    }

    internal companion object {

        internal val Initial: ItemDetailsMenuState = ItemDetailsMenuState(
            action = BottomSheetItemAction.None,
            event = ItemDetailsMenuEvent.Idle,
            itemOption = None,
            itemActionsOption = None,
            shareOption = None
        )

    }

}

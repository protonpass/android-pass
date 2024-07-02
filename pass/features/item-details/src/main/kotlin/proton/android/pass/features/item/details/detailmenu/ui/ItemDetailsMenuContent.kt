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
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.pin
import proton.android.pass.composecomponents.impl.bottomsheet.unpin
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.item.details.detailmenu.presentation.ItemDetailsMenuState

@Composable
internal fun ItemDetailsMenuContent(
    modifier: Modifier = Modifier,
    onEvent: (ItemDetailsMenuUiEvent) -> Unit,
    state: ItemDetailsMenuState
) = with(state) {
    when (itemCategory) {
        ItemCategory.Login -> mutableListOf<BottomSheetItem>().apply {

        }

        ItemCategory.Alias,
        ItemCategory.Note,
        ItemCategory.CreditCard,
        ItemCategory.Identity -> mutableListOf<BottomSheetItem>().apply {
            if (isItemPinned) {
                add(
                    unpin(
                        action = action,
                        onClick = { onEvent(ItemDetailsMenuUiEvent.OnUnpinItemClicked) }
                    )
                )
            } else {
                add(
                    pin(
                        action = action,
                        onClick = { onEvent(ItemDetailsMenuUiEvent.OnPinItemClicked) }
                    )
                )
            }
        }

        ItemCategory.Password,
        ItemCategory.Unknown -> emptyList()
    }.also { bottomSheetItems ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = bottomSheetItems.withDividers().toPersistentList()
        )
    }
}

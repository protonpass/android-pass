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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.delete
import proton.android.pass.composecomponents.impl.bottomsheet.leave
import proton.android.pass.composecomponents.impl.bottomsheet.restore
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.icon.PassItemIcon
import proton.android.pass.features.item.trash.trashmenu.presentation.ItemTrashMenuState

@Composable
internal fun ItemTrashMenuContent(
    modifier: Modifier = Modifier,
    onEvent: (ItemTrashMenuUiEvent) -> Unit,
    state: ItemTrashMenuState
) = with(state) {
    Column(
        modifier = modifier.bottomSheet()
    ) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemTitle) },
            subtitle = { BottomSheetItemSubtitle(text = itemSubtitle) },
            leftIcon = {
                PassItemIcon(
                    itemCategory = itemCategory,
                    text = itemTitle,
                    website = itemWebsite,
                    packageName = itemPackageName,
                    canLoadExternalImages = canLoadExternalImages
                )
            }
        )

        Divider(modifier = Modifier.fillMaxWidth())

        buildList {
            if (canBeLeft) {
                leave(
                    onClick = { onEvent(ItemTrashMenuUiEvent.OnLeaveItemClicked) }
                ).also(::add)
            }

            if (canBeDeleted) {
                restore(
                    action = action,
                    onClick = { onEvent(ItemTrashMenuUiEvent.OnRestoreItemClicked) }
                ).also(::add)

                delete(
                    onClick = { onEvent(ItemTrashMenuUiEvent.OnDeleteItemPermanentlyClicked) }
                ).also(::add)
            }
        }.let { bottomSheetItems ->
            BottomSheetItemList(
                items = bottomSheetItems.withDividers().toPersistentList()
            )
        }
    }
}

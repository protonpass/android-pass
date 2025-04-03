/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.trash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.delete
import proton.android.pass.composecomponents.impl.bottomsheet.leave
import proton.android.pass.composecomponents.impl.bottomsheet.restore
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

@Composable
fun TrashItemBottomSheetContents(
    modifier: Modifier = Modifier,
    canBeDeleted: Boolean,
    itemUiModel: ItemUiModel,
    onLeaveItem: (ItemUiModel) -> Unit,
    onRestoreItem: (ItemUiModel) -> Unit,
    onDeleteItem: (ItemUiModel) -> Unit,
    icon: @Composable () -> Unit
) {
    Column(modifier.bottomSheet()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.contents.title) },
            subtitle = {
                val text = when (val itemType = itemUiModel.contents) {
                    is ItemContents.Alias -> itemType.aliasEmail
                    is ItemContents.Login -> itemType.itemEmail
                    is ItemContents.Note -> itemType.note.replace("\n", " ")
                    else -> ""
                }
                BottomSheetItemSubtitle(text = text)
            },
            leftIcon = {
                icon()
            }
        )

        Divider(modifier = Modifier.fillMaxWidth())

        buildList {
            if (itemUiModel.isSharedWithMe) {
                leave(
                    onClick = { onLeaveItem(itemUiModel) }
                ).also(::add)
            }

            if (canBeDeleted) {
                restore(
                    action = BottomSheetItemAction.None,
                    onClick = { onRestoreItem(itemUiModel) }
                ).also(::add)

                delete(
                    onClick = { onDeleteItem(itemUiModel) }
                ).also(::add)
            }
        }.let { items ->
            BottomSheetItemList(
                items = items.withDividers().toPersistentList()
            )
        }
    }
}

@[Preview Composable]
internal fun TrashItemBottomSheetContentsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TrashItemBottomSheetContents(
                canBeDeleted = true,
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    userId = UserId(id = ""),
                    contents = ItemContents.Alias(
                        title = "My Alias",
                        note = "Note",
                        aliasEmail = "alias.email@proton.me"
                    ),
                    createTime = Clock.System.now(),
                    state = 0,
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now(),
                    isPinned = false,
                    pinTime = Clock.System.now(),
                    revision = 1,
                    shareCount = 0,
                    shareType = ShareType.Vault
                ),
                onLeaveItem = { },
                onRestoreItem = { },
                onDeleteItem = { },
                icon = { AliasIcon() }
            )
        }
    }
}

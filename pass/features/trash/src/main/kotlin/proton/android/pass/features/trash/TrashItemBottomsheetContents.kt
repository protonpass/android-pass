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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import me.proton.core.presentation.R as CoreR

@Composable
fun TrashItemBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
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

        BottomSheetItemList(
            items = persistentListOf(
                restoreItem(itemUiModel, onRestoreItem),
                bottomSheetDivider(),
                deleteItem(itemUiModel, onDeleteItem)
            )
        )
    }
}

private fun restoreItem(item: ItemUiModel, onRestoreItem: (ItemUiModel) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.trash_action_restore)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_clock_rotate_left) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onRestoreItem(item) }
        override val isDivider = false
    }

private fun deleteItem(item: ItemUiModel, onDeleteItem: (ItemUiModel) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.bottomsheet_delete_permanently),
                    color = ProtonTheme.colors.notificationError
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(
                    iconId = CoreR.drawable.ic_proton_trash_cross,
                    tint = ProtonTheme.colors.notificationError
                )
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onDeleteItem(item) }
        override val isDivider = false
    }

@[Preview Composable]
internal fun TrashItemBottomSheetContentsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            TrashItemBottomSheetContents(
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
                    revision = 1,
                    shareCount = 0,
                    isOwner = true
                ),
                onRestoreItem = { },
                onDeleteItem = { },
                icon = { AliasIcon() }
            )
        }
    }
}

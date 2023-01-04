package me.proton.pass.presentation.trash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import me.proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import me.proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import me.proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R

@ExperimentalMaterialApi
@Composable
fun TrashItemBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    onRestoreItem: (ItemUiModel) -> Unit,
    onDeleteItem: (ItemUiModel) -> Unit
) {
    Column(modifier) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.name) },
            subtitle = {
                val text = when (val itemType = itemUiModel.itemType) {
                    is ItemType.Alias -> itemType.aliasEmail
                    is ItemType.Login -> itemType.username
                    is ItemType.Note -> itemType.text.replace("\n", " ")
                    else -> ""
                }
                BottomSheetItemSubtitle(text = text)
            },
            icon = {
                when (itemUiModel.itemType) {
                    is ItemType.Alias -> AliasIcon()
                    is ItemType.Login -> LoginIcon()
                    is ItemType.Note -> NoteIcon()
                    else -> {}
                }
            }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = persistentListOf(
                restoreItem(itemUiModel, onRestoreItem),
                deleteItem(itemUiModel, onDeleteItem)
            )
        )
    }
}

private fun restoreItem(
    itemUiModel: ItemUiModel,
    onRestoreItem: (ItemUiModel) -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.action_restore)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_clock_rotate_left) }
    override val onClick: () -> Unit
        get() = { onRestoreItem(itemUiModel) }
}

private fun deleteItem(
    itemUiModel: ItemUiModel,
    onDeleteItem: (ItemUiModel) -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = R.string.bottomsheet_delete_permanently),
                textcolor = ProtonTheme.colors.notificationError
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = {
            BottomSheetItemIcon(
                iconId = me.proton.core.presentation.R.drawable.ic_proton_trash_cross,
                tint = ProtonTheme.colors.notificationError
            )
        }
    override val onClick: () -> Unit
        get() = { onDeleteItem(itemUiModel) }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun TrashItemBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            TrashItemBottomSheetContents(
                Modifier,
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Alias",
                    note = "Note",
                    itemType = ItemType.Alias("alias.email@proton.me")
                ),
                {},
                {}
            )
        }
    }
}


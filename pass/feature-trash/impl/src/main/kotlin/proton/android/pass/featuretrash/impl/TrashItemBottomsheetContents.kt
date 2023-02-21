package proton.android.pass.featuretrash.impl

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
import kotlinx.datetime.Clock
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonui.api.PassDimens.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun TrashItemBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    onRestoreItem: (ItemUiModel) -> Unit,
    onDeleteItem: (ItemUiModel) -> Unit
) {
    Column(modifier.bottomSheetPadding()) {
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
                    is ItemType.Login -> LoginIcon(text = itemUiModel.name)
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
    override val isDivider = false
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
    override val isDivider = false
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
                    itemType = ItemType.Alias("alias.email@proton.me"),
                    modificationTime = Clock.System.now()
                ),
                {},
                {}
            )
        }
    }
}


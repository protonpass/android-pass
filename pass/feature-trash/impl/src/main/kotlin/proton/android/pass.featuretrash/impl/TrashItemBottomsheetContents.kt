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
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.featuretrash.R
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import me.proton.core.presentation.R as CoreR

@ExperimentalMaterialApi
@Composable
fun TrashItemBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    onRestoreItem: (ShareId, ItemId) -> Unit,
    onDeleteItem: (ShareId, ItemId) -> Unit
) {
    Column(modifier.bottomSheet()) {
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
            leftIcon = {
                when (val itemType = itemUiModel.itemType) {
                    is ItemType.Alias -> AliasIcon()
                    is ItemType.Login -> LoginIcon(
                        text = itemUiModel.name,
                        itemType = itemType
                    )
                    is ItemType.Note -> NoteIcon()
                    else -> {}
                }
            }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = persistentListOf(
                restoreItem(itemUiModel.shareId, itemUiModel.id, onRestoreItem),
                bottomSheetDivider(),
                deleteItem(itemUiModel.shareId, itemUiModel.id, onDeleteItem)
            )
        )
    }
}

private fun restoreItem(
    shareId: ShareId,
    itemId: ItemId,
    onRestoreItem: (ShareId, ItemId) -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.trash_action_restore)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_clock_rotate_left) }
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: () -> Unit
        get() = { onRestoreItem(shareId, itemId) }
    override val isDivider = false
}

private fun deleteItem(
    shareId: ShareId,
    itemId: ItemId,
    onDeleteItem: (ShareId, ItemId) -> Unit
): BottomSheetItem = object : BottomSheetItem {
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
        get() = { onDeleteItem(shareId, itemId) }
    override val isDivider = false
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun TrashItemBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrashItemBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Alias",
                    note = "Note",
                    itemType = ItemType.Alias("alias.email@proton.me"),
                    createTime = Clock.System.now(),
                    state = 0,
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now()
                ),
                onRestoreItem = { _: ShareId, _: ItemId -> },
                onDeleteItem = { _: ShareId, _: ItemId -> },
            )
        }
    }
}

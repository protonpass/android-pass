package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetDivider
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.featurehome.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun AliasOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    onCopyAlias: (String) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit
) {
    val itemType = itemUiModel.itemType as ItemType.Alias
    Column(modifier.bottomSheetPadding()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.name) },
            subtitle = {
                BottomSheetItemSubtitle(
                    text = itemType.aliasEmail
                )
            },
            icon = { AliasIcon() }
        )
        BottomSheetItemList(
            items = persistentListOf(
                copyAlias(itemType.aliasEmail, onCopyAlias),
                BottomSheetDivider(),
                edit(itemUiModel, onEdit),
                moveToTrash(itemUiModel, onMoveToTrash)
            )
        )
    }
}

private fun copyAlias(aliasEmail: String, onCopyAlias: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_alias)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val icon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val onClick: () -> Unit
            get() = { onCopyAlias(aliasEmail) }
        override val isDivider = false
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun AliasOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AliasOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Alias",
                    note = "Note content",
                    itemType = ItemType.Alias("alias.email@proton.me"),
                    modificationTime = Clock.System.now()
                ),
                onCopyAlias = {},
                onEdit = { _, _ -> },
                onMoveToTrash = {}
            )
        }
    }
}


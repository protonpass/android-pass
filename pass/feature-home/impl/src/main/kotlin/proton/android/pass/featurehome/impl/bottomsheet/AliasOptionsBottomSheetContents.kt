package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
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
    isRecentSearch: Boolean = false,
    onCopyAlias: (String) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit,
    onRemoveFromRecentSearch: (ShareId, ItemId) -> Unit
) {
    val itemType = itemUiModel.itemType as ItemType.Alias
    Column(modifier.bottomSheet()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.name) },
            subtitle = {
                BottomSheetItemSubtitle(
                    text = itemType.aliasEmail
                )
            },
            leftIcon = { AliasIcon() }
        )
        val list = mutableListOf(
            copyAlias(itemType.aliasEmail, onCopyAlias),
            edit(itemUiModel, onEdit),
            moveToTrash(itemUiModel, onMoveToTrash)
        )
        if (isRecentSearch) {
            list.add(removeFromRecentSearch(itemUiModel, onRemoveFromRecentSearch))
        }
        BottomSheetItemList(
            items = list.withDividers().toPersistentList()
        )
    }
}

private fun copyAlias(aliasEmail: String, onCopyAlias: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_alias)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCopyAlias(aliasEmail) }
        override val isDivider = false
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun AliasOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Alias",
                    note = "Note content",
                    itemType = ItemType.Alias("alias.email@proton.me"),
                    state = 0,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now()
                ),
                isRecentSearch = input.second,
                onCopyAlias = {},
                onEdit = { _, _ -> },
                onMoveToTrash = {},
                onRemoveFromRecentSearch = { _, _ -> }
            )
        }
    }
}


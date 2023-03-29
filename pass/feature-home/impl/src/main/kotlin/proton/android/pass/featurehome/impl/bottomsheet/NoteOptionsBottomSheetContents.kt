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
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.featurehome.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun NoteOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    isRecentSearch: Boolean = false,
    onCopyNote: (String) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit,
    onRemoveFromRecentSearch: (ShareId, ItemId) -> Unit
) {
    val itemType = itemUiModel.itemType as ItemType.Note
    Column(modifier.bottomSheetPadding()) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.name) },
            subtitle = {
                val processedText = itemType.text.replace("\n", " ")
                BottomSheetItemSubtitle(
                    text = processedText
                )
            },
            leftIcon = { NoteIcon() }
        )
        val list = mutableListOf(
            copyNote(itemType.text, onCopyNote),
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

private fun copyNote(text: String, onCopyNote: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_note)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onCopyNote(text) }
        override val isDivider = false
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun NoteOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            NoteOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Note",
                    note = "Note content",
                    itemType = ItemType.Note("My note text"),
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = Clock.System.now()
                ),
                isRecentSearch = input.second,
                onCopyNote = {},
                onEdit = { _, _ -> },
                onMoveToTrash = {},
                onRemoveFromRecentSearch = { _, _ -> }
            )
        }
    }
}

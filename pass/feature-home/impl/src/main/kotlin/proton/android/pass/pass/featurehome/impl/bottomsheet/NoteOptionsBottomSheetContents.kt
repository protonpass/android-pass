package proton.android.pass.pass.featurehome.impl.bottomsheet

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
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.pass.featurehome.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@ExperimentalMaterialApi
@Composable
fun NoteOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    onCopyNote: (String) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit
) {
    val itemType = itemUiModel.itemType as ItemType.Note
    Column(modifier) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.name) },
            subtitle = {
                val processedText = itemType.text.replace("\n", " ")
                BottomSheetItemSubtitle(
                    text = processedText
                )
            },
            icon = { NoteIcon() }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = persistentListOf(
                copyNote(itemType.text, onCopyNote),
                edit(itemUiModel, onEdit),
                moveToTrash(itemUiModel, onMoveToTrash)
            )
        )
    }
}

private fun copyNote(text: String, onCopyNote: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_note)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val icon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val onClick: () -> Unit
            get() = { onCopyNote(text) }
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun NoteOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            NoteOptionsBottomSheetContents(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Note",
                    note = "Note content",
                    itemType = ItemType.Note("My note text")
                ),
                onCopyNote = {},
                onEdit = { _, _ -> },
                onMoveToTrash = {}
            )
        }
    }
}


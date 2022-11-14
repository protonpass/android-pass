package me.proton.pass.presentation.components.common.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.icon.NoteIcon
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.NoteItemParameter
import me.proton.pass.presentation.components.previewproviders.NoteItemPreviewProvider

@Composable
fun NoteRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    itemType: ItemType.Note,
    highlight: String? = null
) {
    val processedText = itemType.text.replace("\n", " ")
    ItemRow(
        icon = { NoteIcon() },
        title = item.name.highlight(highlight),
        subtitle = processedText.highlight(highlight),
        modifier = modifier
    )
}

class ThemedNoteItemPreviewProvider : ThemePairPreviewProvider<NoteItemParameter>(
    NoteItemPreviewProvider()
)

@Preview
@Composable
fun NoteRowPreview(
    @PreviewParameter(ThemedNoteItemPreviewProvider::class) input: Pair<Boolean, NoteItemParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            NoteRow(
                item = input.second.model,
                itemType = input.second.itemType
            )
        }
    }
}

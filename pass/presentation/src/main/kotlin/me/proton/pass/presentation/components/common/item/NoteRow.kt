package me.proton.pass.presentation.components.common.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.icon.NoteIcon
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.NoteRowParameter
import me.proton.pass.presentation.components.previewproviders.NoteRowPreviewProvider

@Composable
fun NoteRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = ""
) {
    require(item.itemType is ItemType.Note)
    val processedText = item.itemType.text.replace("\n", " ")
    var title = AnnotatedString(item.name)
    var note = AnnotatedString(processedText)
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
        val titleMatches = regex.findAll(item.name)
        if (titleMatches.any()) {
            title = item.name.highlight(titleMatches)
        }
        val noteMatches = regex.findAll(processedText)
        if (noteMatches.any()) {
            note = processedText.highlight(noteMatches)
        }
    }

    ItemRow(
        icon = { NoteIcon() },
        title = title,
        subtitles = persistentListOf(note),
        modifier = modifier
    )
}

class ThemedNoteItemPreviewProvider : ThemePairPreviewProvider<NoteRowParameter>(
    NoteRowPreviewProvider()
)

@Preview
@Composable
fun NoteRowPreview(
    @PreviewParameter(ThemedNoteItemPreviewProvider::class) input: Pair<Boolean, NoteRowParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            NoteRow(
                item = input.second.model,
                highlight = input.second.highlight
            )
        }
    }
}

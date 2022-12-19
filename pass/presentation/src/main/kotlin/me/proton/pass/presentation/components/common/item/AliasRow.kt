package me.proton.pass.presentation.components.common.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.icon.AliasIcon
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.AliasRowParameter
import me.proton.pass.presentation.components.previewproviders.AliasRowPreviewProvider

@Composable
internal fun AliasRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = ""
) {
    require(item.itemType is ItemType.Alias)
    var title = AnnotatedString(item.name)
    var aliasEmail = AnnotatedString(item.itemType.aliasEmail)
    var note: AnnotatedString? = null
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
        val titleMatches = regex.findAll(item.name)
        if (titleMatches.any()) {
            title = item.name.highlight(titleMatches)
        }
        val aliasEmailMatches = regex.findAll(item.itemType.aliasEmail)
        if (aliasEmailMatches.any()) {
            aliasEmail = item.itemType.aliasEmail.highlight(aliasEmailMatches)
        }
        val cleanNote = item.note.replace("\n", " ")
        val noteMatches = regex.findAll(cleanNote)
        if (noteMatches.any()) {
            note = cleanNote.highlight(noteMatches)
        }
    }

    ItemRow(
        icon = { AliasIcon() },
        title = title,
        subtitles = listOfNotNull(aliasEmail, note),
        modifier = modifier
    )
}

class ThemedAliasItemPreviewProvider : ThemePairPreviewProvider<AliasRowParameter>(
    AliasRowPreviewProvider()
)

@Preview
@Composable
fun AliasRowPreview(
    @PreviewParameter(ThemedAliasItemPreviewProvider::class) input: Pair<Boolean, AliasRowParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            AliasRow(
                item = input.second.model,
                highlight = input.second.highlight
            )
        }
    }
}

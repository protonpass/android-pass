package me.proton.android.pass.composecomponents.impl.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toImmutableList
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.domain.ItemType

@Composable
internal fun AliasRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = ""
) {
    with(item.itemType as ItemType.Alias) {
        var title = AnnotatedString(item.name)
        var aliasEmail = AnnotatedString(this.aliasEmail)
        var note: AnnotatedString? = null
        if (highlight.isNotBlank()) {
            val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
            val titleMatches = regex.findAll(item.name)
            if (titleMatches.any()) {
                title = item.name.highlight(titleMatches)
            }
            val aliasEmailMatches = regex.findAll(this.aliasEmail)
            if (aliasEmailMatches.any()) {
                aliasEmail = this.aliasEmail.highlight(aliasEmailMatches)
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
            subtitles = listOfNotNull(aliasEmail, note).toImmutableList(),
            modifier = modifier
        )
    }
}

class ThemedAliasItemPreviewProvider :
    ThemePairPreviewProvider<AliasRowParameter>(AliasRowPreviewProvider())

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

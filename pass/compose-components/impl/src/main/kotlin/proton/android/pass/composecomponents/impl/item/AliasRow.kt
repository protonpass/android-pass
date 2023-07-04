/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.composecomponents.impl.item

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.pass.domain.ItemContents

private const val MAX_PREVIEW_LENGTH = 128

@Composable
internal fun AliasRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null
) {
    val content = item.contents as ItemContents.Alias

    val highlightColor = PassTheme.colors.interactionNorm
    val fields = remember(content.title, content.aliasEmail, content.note, highlight) {
        getHighlightedFields(
            title = content.title,
            aliasEmail = content.aliasEmail,
            note = content.note,
            highlight = highlight,
            highlightColor = highlightColor
        )
    }

    ItemRow(
        modifier = modifier,
        icon = { AliasIcon() },
        title = fields.title,
        subtitles = listOfNotNull(fields.aliasEmail, fields.note).toImmutableList(),
        vaultIcon = vaultIcon
    )
}

private fun getHighlightedFields(
    title: String,
    aliasEmail: String,
    note: String,
    highlight: String,
    highlightColor: Color
): AliasHighlightFields {
    var annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))
    var annotatedAliasEmail = AnnotatedString(aliasEmail)
    var annotatedNote: AnnotatedString? = null
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.LITERAL))
        val titleMatches = regex.findAll(title)
        if (titleMatches.any()) {
            annotatedTitle = title.highlight(titleMatches, highlightColor)
        }
        val aliasEmailMatches = regex.findAll(aliasEmail)
        if (aliasEmailMatches.any()) {
            annotatedAliasEmail = aliasEmail.highlight(aliasEmailMatches, highlightColor)
        }
        val cleanNote = note.replace("\n", " ")
        val noteMatches = regex.findAll(cleanNote)
        if (noteMatches.any()) {
            annotatedNote = cleanNote.highlight(noteMatches, highlightColor)
        }
    }

    return AliasHighlightFields(
        title = annotatedTitle,
        aliasEmail = annotatedAliasEmail,
        note = annotatedNote
    )
}

@Stable
private data class AliasHighlightFields(
    val title: AnnotatedString,
    val aliasEmail: AnnotatedString,
    val note: AnnotatedString?,
)

class ThemedAliasItemPreviewProvider :
    ThemePairPreviewProvider<AliasRowParameter>(AliasRowPreviewProvider())

@Preview
@Composable
fun AliasRowPreview(
    @PreviewParameter(ThemedAliasItemPreviewProvider::class) input: Pair<Boolean, AliasRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasRow(
                item = input.second.model,
                highlight = input.second.highlight
            )
        }
    }
}

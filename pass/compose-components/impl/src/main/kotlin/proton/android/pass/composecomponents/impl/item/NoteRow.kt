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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.pass.domain.ItemContents

private const val MAX_LINES_NOTE_DETAIL = 1

@Composable
fun NoteRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null
) {
    val content = item.contents as ItemContents.Note
    var title = AnnotatedString(content.title)
    val note = if (highlight.isNotBlank()) {
        val processedText = content.note.replace("\n", " ")
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
        val titleMatches = regex.findAll(content.title)
        if (titleMatches.any()) {
            title = content.title.highlight(titleMatches)
        }
        val noteMatches = regex.findAll(processedText)
        if (noteMatches.any()) {
            processedText.highlight(noteMatches)
        } else {
            AnnotatedString(processedText)
        }
    } else {
        val firstLines = content.note.lines().take(MAX_LINES_NOTE_DETAIL)
        AnnotatedString(firstLines.joinToString(" "))
    }

    ItemRow(
        modifier = modifier,
        icon = { NoteIcon() },
        title = title,
        subtitles = persistentListOf(note),
        vaultIcon = vaultIcon
    )
}

class ThemedNoteItemPreviewProvider :
    ThemePairPreviewProvider<NoteRowParameter>(NoteRowPreviewProvider())

@Preview
@Composable
fun NoteRowPreview(
    @PreviewParameter(ThemedNoteItemPreviewProvider::class) input: Pair<Boolean, NoteRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            NoteRow(
                item = input.second.model,
                highlight = input.second.highlight
            )
        }
    }
}

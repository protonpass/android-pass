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
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.pass.domain.ItemContents

private const val MAX_PREVIEW_LENGTH = 128

@Composable
fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    canLoadExternalImages: Boolean,
) {
    val content = item.contents as ItemContents.Login

    val highlightColor = PassTheme.colors.interactionNorm
    val fields = remember(content.title, content.username, content.note, content.urls, highlight) {
        getHighlightedFields(
            title = content.title,
            username = content.username,
            note = content.note,
            urls = content.urls,
            highlight = highlight,
            highlightColor = highlightColor
        )
    }

    ItemRow(
        modifier = modifier,
        icon = {
            LoginIcon(
                text = fields.title.text,
                content = content,
                canLoadExternalImages = canLoadExternalImages
            )
        },
        title = fields.title,
        subtitles = (
            listOfNotNull(
                fields.username,
                fields.note
            ) + fields.websites
            ).toImmutableList(),
        vaultIcon = vaultIcon
    )
}

@Suppress("LongParameterList")
private fun getHighlightedFields(
    title: String,
    username: String,
    note: String,
    urls: List<String>,
    highlight: String,
    highlightColor: Color
): LoginHighlightFields {
    var annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))
    var annotatedUsername = AnnotatedString(username.take(MAX_PREVIEW_LENGTH))
    var annotatedNote: AnnotatedString? = null
    val annotatedWebsites: MutableList<AnnotatedString> = mutableListOf()
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.LITERAL))
        val titleMatches = regex.findAll(title)
        if (titleMatches.any()) {
            annotatedTitle = title.highlight(titleMatches, highlightColor)
        }
        val usernameMatches = regex.findAll(username)
        if (usernameMatches.any()) {
            annotatedUsername = username.highlight(usernameMatches, highlightColor)
        }
        val cleanNote = note.replace("\n", " ")
        val noteMatches = regex.findAll(cleanNote)
        if (noteMatches.any()) {
            annotatedNote = cleanNote.highlight(noteMatches, highlightColor)
        }
        urls.forEach {
            val websiteMatch = regex.findAll(it)
            if (websiteMatch.any()) {
                annotatedWebsites.add(it.highlight(websiteMatch, highlightColor))
            }
            if (annotatedWebsites.size >= 2) return@forEach
        }
    }

    return LoginHighlightFields(
        title = annotatedTitle,
        note = annotatedNote,
        username = annotatedUsername,
        websites = annotatedWebsites
    )
}

@Stable
private data class LoginHighlightFields(
    val title: AnnotatedString,
    val note: AnnotatedString?,
    val username: AnnotatedString,
    val websites: List<AnnotatedString>
)

class ThemedLoginItemPreviewProvider : ThemePairPreviewProvider<LoginRowParameter>(
    LoginRowPreviewProvider()
)

@Preview
@Composable
fun LoginRowPreview(
    @PreviewParameter(ThemedLoginItemPreviewProvider::class) input: Pair<Boolean, LoginRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LoginRow(
                item = input.second.model,
                highlight = input.second.highlight,
                canLoadExternalImages = false
            )
        }
    }
}

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
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.pass.domain.ItemContents

@Composable
internal fun AliasRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null
) {
    val content = item.contents as ItemContents.Alias
    var title = AnnotatedString(content.title)
    var aliasEmail = AnnotatedString(content.aliasEmail)
    var note: AnnotatedString? = null
    if (highlight.isNotBlank()) {
        val regex = highlight.toRegex(setOf(RegexOption.IGNORE_CASE))
        val titleMatches = regex.findAll(content.title)
        if (titleMatches.any()) {
            title = content.title.highlight(titleMatches)
        }
        val aliasEmailMatches = regex.findAll(content.aliasEmail)
        if (aliasEmailMatches.any()) {
            aliasEmail = content.aliasEmail.highlight(aliasEmailMatches)
        }
        val cleanNote = content.note.replace("\n", " ")
        val noteMatches = regex.findAll(cleanNote)
        if (noteMatches.any()) {
            note = cleanNote.highlight(noteMatches)
        }
    }

    ItemRow(
        modifier = modifier,
        icon = { AliasIcon() },
        title = title,
        subtitles = listOfNotNull(aliasEmail, note).toImmutableList(),
        vaultIcon = vaultIcon
    )
}

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

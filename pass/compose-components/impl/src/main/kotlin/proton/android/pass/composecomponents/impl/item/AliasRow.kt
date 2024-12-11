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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.domain.ItemContents

private const val MAX_PREVIEW_LENGTH = 128

@Composable
internal fun AliasRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    selection: ItemSelectionModeState = ItemSelectionModeState.NotInSelectionMode,
    titleSuffix: Option<String>
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
        icon = {
            when (selection) {
                ItemSelectionModeState.NotInSelectionMode -> OverlayBadge(
                    isShown = item.isPinned,
                    badge = {
                        CircledBadge(
                            ratio = 0.8f,
                            backgroundColor = PassTheme.colors.aliasInteractionNormMajor1
                        )
                    },
                    content = { AliasIcon(activeAlias = content.isEnabled) }
                )

                is ItemSelectionModeState.InSelectionMode -> {
                    if (selection.state == ItemSelectionModeState.ItemSelectionState.Selected) {
                        ItemSelectedIcon(Modifier.padding(end = 6.dp))
                    } else {
                        val isEnabled =
                            selection.state != ItemSelectionModeState.ItemSelectionState.NotSelectable
                        OverlayBadge(
                            isShown = item.isPinned,
                            badge = {
                                CircledBadge(
                                    ratio = 0.8f,
                                    backgroundColor = PassTheme.colors.aliasInteractionNormMajor1
                                )
                            },
                            content = {
                                AliasIcon(
                                    enabled = isEnabled,
                                    activeAlias = content.isEnabled
                                )
                            }
                        )
                    }
                }
            }
        },
        title = fields.title,
        titleSuffix = titleSuffix,
        subtitles = fields.subtitles,
        vaultIcon = vaultIcon,
        enabled = selection.isSelectable(),
        isShared = item.isShared
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
        title.highlight(highlight, highlightColor)?.let {
            annotatedTitle = it
        }
        aliasEmail.highlight(highlight, highlightColor)?.let {
            annotatedAliasEmail = it
        }
        note.replace("\n", " ").highlight(highlight, highlightColor)?.let {
            annotatedNote = it
        }
    }

    return AliasHighlightFields(
        title = annotatedTitle,
        aliasEmail = annotatedAliasEmail,
        note = annotatedNote,
        subtitles = listOfNotNull(annotatedAliasEmail, annotatedNote).toImmutableList()
    )
}

@Stable
private data class AliasHighlightFields(
    val title: AnnotatedString,
    val aliasEmail: AnnotatedString,
    val note: AnnotatedString?,
    val subtitles: ImmutableList<AnnotatedString>
)

class ThemedAliasItemPreviewProvider :
    ThemePairPreviewProvider<AliasRowParameter>(AliasRowPreviewProvider())

@Preview
@Composable
fun AliasRowPreview(@PreviewParameter(ThemedAliasItemPreviewProvider::class) input: Pair<Boolean, AliasRowParameter>) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasRow(
                item = input.second.model,
                highlight = input.second.highlight,
                titleSuffix = None
            )
        }
    }
}

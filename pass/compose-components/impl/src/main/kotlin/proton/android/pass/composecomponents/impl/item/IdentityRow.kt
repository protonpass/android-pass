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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.pinning.BoxedPin
import proton.android.pass.composecomponents.impl.pinning.CircledPin
import proton.android.pass.domain.ItemContents

private const val MAX_PREVIEW_LENGTH = 128

@Composable
fun IdentityRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    selection: ItemSelectionModeState = ItemSelectionModeState.NotInSelectionMode
) {
    val content = item.contents as ItemContents.Identity

    val (title) = remember(content.title, highlight) {
        getHighlightedFields(content.title)
    }

    ItemRow(
        modifier = modifier,
        icon = {
            when (selection) {
                ItemSelectionModeState.NotInSelectionMode -> BoxedPin(
                    isShown = item.isPinned,
                    pin = {
                        CircledPin(
                            ratio = 0.8f,
                            backgroundColor = PassTheme.colors.interactionNormMajor1
                        )
                    },
                    content = { IdentityIcon() }
                )

                is ItemSelectionModeState.InSelectionMode -> {
                    if (selection.state == ItemSelectionModeState.ItemSelectionState.Selected) {
                        ItemSelectedIcon(Modifier.padding(end = 6.dp))
                    } else {
                        val isEnabled =
                            selection.state != ItemSelectionModeState.ItemSelectionState.NotSelectable
                        BoxedPin(
                            isShown = item.isPinned,
                            pin = {
                                CircledPin(
                                    ratio = 0.8f,
                                    backgroundColor = PassTheme.colors.interactionNormMajor1
                                )
                            },
                            content = { IdentityIcon(enabled = isEnabled) }
                        )
                    }
                }
            }
        },
        title = title,
        subtitles = persistentListOf(),
        vaultIcon = vaultIcon,
        enabled = selection.isSelectable()
    )
}

private fun getHighlightedFields(title: String): IdentityHighlightFields {
    val annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))

    return IdentityHighlightFields(annotatedTitle)
}

@Stable
private data class IdentityHighlightFields(
    val title: AnnotatedString
)

class ThemedIdentityItemPreviewProvider :
    ThemePairPreviewProvider<IdentityRowParameter>(IdentityRowPreviewProvider())

@Preview
@Composable
fun IdentityRowPreview(
    @PreviewParameter(ThemedIdentityItemPreviewProvider::class) input: Pair<Boolean, IdentityRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            IdentityRow(
                item = input.second.model,
                highlight = input.second.highlight
            )
        }
    }
}

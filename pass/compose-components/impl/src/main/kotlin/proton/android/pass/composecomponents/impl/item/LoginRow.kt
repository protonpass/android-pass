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
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemContents

private const val MAX_PREVIEW_LENGTH = 128

@Composable
fun LoginRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    canLoadExternalImages: Boolean,
    selection: ItemSelectionModeState = ItemSelectionModeState.NotInSelectionMode,
    titleSuffix: Option<String> = None
) {
    val content = remember(item.contents) { item.contents as ItemContents.Login }

    val highlightColor = PassTheme.colors.interactionNorm
    val textCustomFields = remember(content.customFields) {
        content.customFields.filterIsInstance<CustomFieldContent.Text>()
    }
    val fields = remember(
        content.title,
        content.itemEmail,
        content.itemUsername,
        content.note,
        content.urls,
        textCustomFields,
        highlight
    ) {
        getHighlightedFields(
            title = content.title,
            email = content.itemEmail,
            username = content.itemUsername,
            displayUsername = content.displayValue,
            note = content.note,
            urls = content.urls,
            customFields = textCustomFields,
            highlight = highlight,
            highlightColor = highlightColor
        )
    }

    ItemRow(
        modifier = modifier,
        icon = {
            if (selection.isSelected()) {
                ItemSelectedIcon(Modifier.padding(end = 6.dp))
            } else {
                val sortedPackages = remember {
                    content.packageInfoSet.sortedBy { it.packageName.value }
                }
                val packageName = remember { sortedPackages.firstOrNull()?.packageName?.value }
                val website = remember { content.urls.firstOrNull() }

                val enabled = remember(selection) {
                    when (selection) {
                        is ItemSelectionModeState.NotInSelectionMode -> true
                        is ItemSelectionModeState.InSelectionMode -> {
                            selection.state != ItemSelectionModeState.ItemSelectionState.NotSelectable
                        }
                    }
                }

                OverlayBadge(
                    isShown = item.isPinned,
                    badge = {
                        CircledBadge(
                            ratio = 0.8f,
                            backgroundColor = PassTheme.colors.loginInteractionNormMajor1
                        )
                    },
                    content = {
                        LoginIcon(
                            text = fields.title.text,
                            canLoadExternalImages = canLoadExternalImages,
                            website = website,
                            packageName = packageName,
                            enabled = enabled
                        )
                    }
                )
            }
        },
        title = fields.title,
        titleSuffix = titleSuffix,
        subtitles = fields.subtitles,
        vaultIcon = vaultIcon,
        enabled = selection.isSelectable()
    )
}

@Suppress("ComplexMethod", "CyclomaticComplexMethod", "LongParameterList")
private fun getHighlightedFields(
    title: String,
    email: String,
    username: String,
    displayUsername: String,
    note: String,
    urls: List<String>,
    customFields: List<CustomFieldContent.Text>,
    highlight: String,
    highlightColor: Color
): LoginHighlightFields {
    var annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))

    var annotatedEmail: AnnotatedString? = null
    var annotatedUsername: AnnotatedString? = null

    var annotatedNote: AnnotatedString? = null
    val annotatedWebsites: MutableList<AnnotatedString> = mutableListOf()
    val annotatedCustomFields: MutableList<AnnotatedString> = mutableListOf()
    if (highlight.isNotBlank()) {
        title.highlight(highlight, highlightColor)?.let {
            annotatedTitle = it
        }
        email.highlight(highlight, highlightColor)?.let {
            annotatedEmail = it
        }
        username.highlight(highlight, highlightColor)?.let {
            annotatedUsername = it
        }
        note.replace("\n", " ").highlight(highlight, highlightColor)?.let {
            annotatedNote = it
        }
        urls.forEach { website ->
            website.highlight(highlight, highlightColor)?.let {
                annotatedWebsites.add(it)
            }
            if (annotatedWebsites.size >= 2) return@forEach
        }

        customFields.forEach { customField ->
            val customFieldText = "${customField.label}: ${customField.value}"
            customFieldText.highlight(highlight, highlightColor)?.let {
                annotatedCustomFields.add(it)
            }

            if (annotatedCustomFields.size >= 2) return@forEach
        }
    }

    val displayUsernameFallback = AnnotatedString(displayUsername.take(MAX_PREVIEW_LENGTH))
    val rowUsername =
        if (highlight.isBlank() && annotatedEmail == null && annotatedUsername == null) {
            displayUsernameFallback
        } else {
            annotatedUsername ?: annotatedEmail ?: displayUsernameFallback
        }


    return LoginHighlightFields(
        title = annotatedTitle,
        note = annotatedNote,
        email = rowUsername,
        username = annotatedUsername,
        websites = annotatedWebsites,
        subtitles = (
            listOfNotNull(
                rowUsername,
                annotatedNote
            ) + annotatedWebsites + annotatedCustomFields
            ).toPersistentList()
    )
}

@Stable
private data class LoginHighlightFields(
    val title: AnnotatedString,
    val note: AnnotatedString?,
    val email: AnnotatedString?,
    val username: AnnotatedString?,
    val websites: List<AnnotatedString>,
    val subtitles: ImmutableList<AnnotatedString>
)

internal class ThemedLoginItemPreviewProvider : ThemePairPreviewProvider<LoginRowParameter>(
    LoginRowPreviewProvider()
)

@Preview
@Composable
internal fun LoginRowPreview(
    @PreviewParameter(ThemedLoginItemPreviewProvider::class) input: Pair<Boolean, LoginRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LoginRow(
                item = input.second.model,
                highlight = input.second.highlight,
                canLoadExternalImages = false,
                titleSuffix = None
            )
        }
    }
}

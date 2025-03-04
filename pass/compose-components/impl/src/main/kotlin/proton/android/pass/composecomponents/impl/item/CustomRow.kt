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
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.item.icon.CustomIcon
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents

private const val MAX_PREVIEW_LENGTH = 128
private const val MAX_CUSTOM_FIELDS = 2

@Composable
fun CustomRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    selection: ItemSelectionModeState = ItemSelectionModeState.NotInSelectionMode,
    titleSuffix: Option<String> = None
) {
    val (title, customFields, sections) = remember(item.contents) {
        when (val contents = item.contents) {
            is ItemContents.Custom -> Triple(
                contents.title,
                contents.customFieldList,
                contents.sectionContentList
            )

            is ItemContents.SSHKey -> Triple(
                contents.title,
                contents.customFieldList,
                contents.sectionContentList
            )

            is ItemContents.WifiNetwork -> Triple(
                contents.title,
                contents.customFieldList,
                contents.sectionContentList
            )

            else -> throw IllegalStateException("Unsupported item type")
        }
    }
    val highlightColor = PassTheme.colors.interactionNorm
    val fields = remember(title, customFields, sections, highlight) {
        getHighlightedFields(
            title = title,
            customFields = customFields,
            extraSectionContentList = sections,
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
                            backgroundColor = PassTheme.colors.interactionNormMajor1
                        )
                    },
                    content = { CustomIcon() }
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
                                    backgroundColor = PassTheme.colors.interactionNormMajor1
                                )
                            },
                            content = { CustomIcon(enabled = isEnabled) }
                        )
                    }
                }
            }
        },
        title = fields.title,
        titleSuffix = titleSuffix,
        subtitles = fields.subtitles.toImmutableList(),
        vaultIcon = vaultIcon,
        enabled = selection.isSelectable(),
        isShared = item.isShared
    )
}

private fun getHighlightedFields(
    title: String,
    customFields: List<CustomFieldContent>,
    extraSectionContentList: List<ExtraSectionContent>,
    highlight: String,
    highlightColor: Color
): CustomHighlightFields {
    var annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))
    val annotatedFields: MutableList<AnnotatedString> = mutableListOf()

    if (highlight.isNotBlank()) {
        title.highlight(highlight, highlightColor)?.let {
            annotatedTitle = it
        }

        val filteredCustomFields = customFields.filterIsInstance<CustomFieldContent.Text>()
            .mapNotNull { customField ->
                customFieldToAnnotatedString(
                    customField,
                    highlight,
                    highlightColor
                )
            }
            .take(MAX_CUSTOM_FIELDS)
        annotatedFields.addAll(filteredCustomFields)

        extraSectionContentList.forEach { extraSectionContent ->
            val extraSectionCustomField =
                extraSectionContent.customFieldList.filterIsInstance<CustomFieldContent.Text>()
                    .mapNotNull { customField ->
                        customFieldToAnnotatedString(
                            customField,
                            highlight,
                            highlightColor
                        )
                    }
                    .take(MAX_CUSTOM_FIELDS)
            annotatedFields.addAll(extraSectionCustomField)
        }
    }

    return CustomHighlightFields(
        title = annotatedTitle,
        subtitles = annotatedFields
    )
}

private fun customFieldToAnnotatedString(
    customField: CustomFieldContent.Text,
    highlight: String,
    highlightColor: Color
): AnnotatedString? {
    val customFieldText = "${customField.label}: ${customField.value}"
    return customFieldText.highlight(highlight, highlightColor)
}


@Stable
private data class CustomHighlightFields(
    val title: AnnotatedString,
    val subtitles: List<AnnotatedString>
)

class ThemedCustomItemPreviewProvider :
    ThemePairPreviewProvider<CustomRowParameter>(CustomRowPreviewProvider())

@Preview
@Composable
fun CustomRowPreview(
    @PreviewParameter(ThemedCustomItemPreviewProvider::class) input: Pair<Boolean, CustomRowParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CustomRow(
                item = input.second.model,
                highlight = input.second.highlight,
                titleSuffix = None
            )
        }
    }
}

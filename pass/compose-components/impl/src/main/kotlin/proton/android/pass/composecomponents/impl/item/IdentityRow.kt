/*
 * Copyright (c) 2023-2026 Proton AG
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
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.domain.highlightableBodyFields

private const val MAX_PREVIEW_LENGTH = 128

@Composable
fun IdentityRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    vaultIcon: Int? = null,
    selection: ItemSelectionModeState = ItemSelectionModeState.NotInSelectionMode,
    titleSuffix: Option<String> = None
) {
    val content = remember(item.contents) { item.contents as ItemContents.Identity }
    val highlightColor = PassTheme.colors.interactionNorm
    val fields = remember(content, highlight) {
        getHighlightedFields(
            title = content.title,
            personalDetailsContent = content.personalDetailsContent,
            addressDetailsContent = content.addressDetailsContent,
            contactDetailsContent = content.contactDetailsContent,
            workDetailsContent = content.workDetailsContent,
            bodyFields = content.highlightableBodyFields(),
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
                    content = { IdentityIcon() }
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
                            content = { IdentityIcon(enabled = isEnabled) }
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

@Suppress("LongParameterList")
private fun getHighlightedFields(
    title: String,
    personalDetailsContent: PersonalDetailsContent,
    addressDetailsContent: AddressDetailsContent,
    contactDetailsContent: ContactDetailsContent,
    workDetailsContent: WorkDetailsContent,
    bodyFields: List<String>,
    highlight: String,
    highlightColor: Color
): IdentityHighlightFields {
    var annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))
    val nameAndEmail = listOf(personalDetailsContent.fullName, personalDetailsContent.email)
        .filter { it.isNotBlank() }
        .joinToString(" / ")
    var annotatedFullNameEmail = AnnotatedString(nameAndEmail.take(MAX_PREVIEW_LENGTH))
    val annotatedFields: MutableList<AnnotatedString> = mutableListOf()

    if (highlight.isNotBlank()) {
        title.highlight(highlight, highlightColor)?.let { annotatedTitle = it }
        nameAndEmail.highlight(highlight, highlightColor)?.let { annotatedFullNameEmail = it }

        sequenceOf(
            personalDetailsContent.firstName,
            personalDetailsContent.middleName,
            personalDetailsContent.lastName,
            personalDetailsContent.birthdate,
            personalDetailsContent.gender,
            personalDetailsContent.phoneNumber,
            addressDetailsContent.organization,
            addressDetailsContent.streetAddress,
            addressDetailsContent.zipOrPostalCode,
            addressDetailsContent.city,
            addressDetailsContent.stateOrProvince,
            addressDetailsContent.countryOrRegion,
            addressDetailsContent.floor,
            addressDetailsContent.county,
            contactDetailsContent.passportNumber,
            contactDetailsContent.licenseNumber,
            contactDetailsContent.website,
            contactDetailsContent.xHandle,
            contactDetailsContent.secondPhoneNumber,
            contactDetailsContent.linkedin,
            contactDetailsContent.reddit,
            contactDetailsContent.facebook,
            contactDetailsContent.yahoo,
            contactDetailsContent.instagram,
            workDetailsContent.company,
            workDetailsContent.jobTitle,
            workDetailsContent.personalWebsite,
            workDetailsContent.workPhoneNumber,
            workDetailsContent.workEmail
        ).forEach { field ->
            field.highlight(highlight, highlightColor)?.let { annotatedFields.add(it) }
        }

        bodyFields.forEach { field ->
            field.highlight(highlight, highlightColor)?.let { annotatedFields.add(it) }
        }
    }

    return IdentityHighlightFields(
        title = annotatedTitle,
        subtitles = listOf(annotatedFullNameEmail) + annotatedFields
    )
}

@Stable
private data class IdentityHighlightFields(
    val title: AnnotatedString,
    val subtitles: List<AnnotatedString>
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
                highlight = input.second.highlight,
                titleSuffix = None
            )
        }
    }
}

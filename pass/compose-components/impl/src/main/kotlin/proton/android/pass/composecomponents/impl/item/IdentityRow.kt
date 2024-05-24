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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.pinning.BoxedPin
import proton.android.pass.composecomponents.impl.pinning.CircledPin
import proton.android.pass.domain.AddressDetails
import proton.android.pass.domain.ContactDetails
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.PersonalDetails
import proton.android.pass.domain.WorkDetails

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
    val highlightColor = PassTheme.colors.interactionNorm
    val fields = remember(
        content.title,
        content.personalDetails,
        content.addressDetails,
        content.contactDetails,
        content.workDetails,
        highlight
    ) {
        getHighlightedFields(
            title = content.title,
            personalDetails = content.personalDetails,
            addressDetails = content.addressDetails,
            contactDetails = content.contactDetails,
            workDetails = content.workDetails,
            highlight = highlight,
            highlightColor = highlightColor
        )
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
        title = fields.title,
        subtitles = fields.subtitles.toImmutableList(),
        vaultIcon = vaultIcon,
        enabled = selection.isSelectable()
    )
}

@Suppress("LongMethod", "LongParameterList")
private fun getHighlightedFields(
    title: String,
    personalDetails: PersonalDetails,
    addressDetails: AddressDetails,
    contactDetails: ContactDetails,
    workDetails: WorkDetails,
    highlight: String,
    highlightColor: Color
): IdentityHighlightFields {
    var annotatedTitle = AnnotatedString(title.take(MAX_PREVIEW_LENGTH))
    val nameAndEmail = personalDetails.fullName + " / " + personalDetails.email
    var annotatedFullNameEmail = AnnotatedString(nameAndEmail.take(MAX_PREVIEW_LENGTH))

    val annotatedFields: MutableList<AnnotatedString> = mutableListOf()

    if (highlight.isNotBlank()) {
        title.highlight(highlight, highlightColor)?.let {
            annotatedTitle = it
        }
        nameAndEmail.highlight(highlight, highlightColor)?.let {
            annotatedFullNameEmail = it
        }

        // Highlight fields for PersonalDetails
        annotatedFields.addAll(
            highlightFields(
                listOf(
                    personalDetails.firstName,
                    personalDetails.middleName,
                    personalDetails.lastName,
                    personalDetails.birthdate,
                    personalDetails.gender,
                    personalDetails.phoneNumber
                ),
                highlight,
                highlightColor
            )
        )

        // Highlight fields for AddressDetails
        annotatedFields.addAll(
            highlightFields(
                listOf(
                    addressDetails.organization,
                    addressDetails.streetAddress,
                    addressDetails.zipOrPostalCode,
                    addressDetails.city,
                    addressDetails.stateOrProvince,
                    addressDetails.countryOrRegion,
                    addressDetails.floor,
                    addressDetails.county
                ),
                highlight,
                highlightColor
            )
        )

        // Highlight fields for ContactDetails
        annotatedFields.addAll(
            highlightFields(
                listOf(
                    contactDetails.socialSecurityNumber,
                    contactDetails.passportNumber,
                    contactDetails.licenseNumber,
                    contactDetails.website,
                    contactDetails.xHandle,
                    contactDetails.secondPhoneNumber,
                    contactDetails.linkedin,
                    contactDetails.reddit,
                    contactDetails.facebook,
                    contactDetails.yahoo,
                    contactDetails.instagram
                ),
                highlight,
                highlightColor
            )
        )

        // Highlight fields for WorkDetails
        annotatedFields.addAll(
            highlightFields(
                listOf(
                    workDetails.company,
                    workDetails.jobTitle,
                    workDetails.personalWebsite,
                    workDetails.workPhoneNumber,
                    workDetails.workEmail
                ),
                highlight,
                highlightColor
            )
        )
    }

    return IdentityHighlightFields(
        title = annotatedTitle,
        subtitles = listOf(annotatedFullNameEmail) + annotatedFields
    )
}

private fun highlightFields(
    fields: List<String>,
    highlight: String,
    highlightColor: Color
): List<AnnotatedString> {
    val annotatedFields: MutableList<AnnotatedString> = mutableListOf()

    if (highlight.isNotBlank()) {
        fields.forEach { fieldValue ->
            if (fieldValue.contains(highlight, ignoreCase = true)) {
                fieldValue.highlight(highlight, highlightColor)?.let {
                    annotatedFields.add(it)
                }
            }
        }
    }

    return annotatedFields
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
                highlight = input.second.highlight
            )
        }
    }
}

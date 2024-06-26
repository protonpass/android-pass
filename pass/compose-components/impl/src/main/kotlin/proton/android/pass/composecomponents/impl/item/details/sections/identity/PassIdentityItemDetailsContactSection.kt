/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.identity

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ContactDetailsContent

@Composable
internal fun PassIdentityItemDetailsContactSection(
    modifier: Modifier = Modifier,
    contactDetailsContent: ContactDetailsContent,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(contactDetailsContent) {
    val sections = mutableListOf<@Composable () -> Unit>()

    if (hasSocialSecurityNumber) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(
                    id = R.string.item_details_identity_section_contact_social_security_number_title
                ),
                subtitle = socialSecurityNumber,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = socialSecurityNumber,
                        field = ItemDetailsFieldType.Plain.SocialSecurityNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasPassportNumber) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_passport_number_title),
                subtitle = passportNumber,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = passportNumber,
                        field = ItemDetailsFieldType.Plain.PassportNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasLicenseNumber) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_license_number_title),
                subtitle = licenseNumber,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = licenseNumber,
                        field = ItemDetailsFieldType.Plain.LicenseNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasWebsite) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_website_title),
                subtitle = website,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = website,
                        field = ItemDetailsFieldType.Plain.Website
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasSecondPhoneNumber) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(
                    id = R.string.item_details_identity_section_contact_secondary_phone_number_title
                ),
                subtitle = secondPhoneNumber,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = secondPhoneNumber,
                        field = ItemDetailsFieldType.Plain.PhoneNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasLinkedin) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_linkedin_title),
                subtitle = linkedin,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = linkedin,
                        field = ItemDetailsFieldType.Plain.LinkedIn
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasXHandle) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_x_handle_title),
                subtitle = xHandle,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = xHandle,
                        field = ItemDetailsFieldType.Plain.XHandle
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasInstagram) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_instagram_title),
                subtitle = instagram,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = instagram,
                        field = ItemDetailsFieldType.Plain.Instagram
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasFacebook) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_facebook_title),
                subtitle = facebook,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = facebook,
                        field = ItemDetailsFieldType.Plain.Facebook
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasReddit) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_reddit_title),
                subtitle = reddit,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = reddit,
                        field = ItemDetailsFieldType.Plain.Reddit
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasYahoo) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_contact_yahoo_title),
                subtitle = yahoo,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = yahoo,
                        field = ItemDetailsFieldType.Plain.Yahoo
                    ).also(onEvent)
                }
            )
        }
    }

    customFields.forEachIndexed { index, customFieldContent ->
        sections.add {
            PassIdentityItemDetailsCustomFieldRow(
                customFieldIndex = index,
                customFieldContent = customFieldContent,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }
    }

    PassIdentityItemDetailsSection(
        modifier = modifier,
        titleResId = R.string.item_details_identity_section_contact_title,
        sections = sections.toPersistentList()
    )
}

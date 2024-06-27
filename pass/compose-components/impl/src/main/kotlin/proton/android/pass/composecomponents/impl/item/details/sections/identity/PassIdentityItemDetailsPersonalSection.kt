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
import proton.android.pass.composecomponents.impl.item.details.sections.identity.shared.rows.PassIdentityItemDetailsCustomFieldRow
import proton.android.pass.composecomponents.impl.item.details.sections.identity.shared.sections.PassIdentityItemDetailsSection
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.PersonalDetailsContent

@Composable
internal fun PassIdentityItemDetailsPersonalSection(
    modifier: Modifier = Modifier,
    personalDetailsContent: PersonalDetailsContent,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(personalDetailsContent) {
    val sections = mutableListOf<@Composable () -> Unit>()

    if (hasFirstName) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_first_name_title),
                subtitle = firstName,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = firstName,
                        field = ItemDetailsFieldType.Plain.FirstName
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasMiddleName) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_middle_name_title),
                subtitle = middleName,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = middleName,
                        field = ItemDetailsFieldType.Plain.MiddleName
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasLastName) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_last_name_title),
                subtitle = lastName,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = lastName,
                        field = ItemDetailsFieldType.Plain.LastName
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasFullName) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_full_name_title),
                subtitle = fullName,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = fullName,
                        field = ItemDetailsFieldType.Plain.FullName
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasEmail) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_email_title),
                subtitle = email,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = email,
                        field = ItemDetailsFieldType.Plain.Email
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasGender) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_gender_title),
                subtitle = gender,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = gender,
                        field = ItemDetailsFieldType.Plain.Gender
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasPhoneNumber) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_phone_title),
                subtitle = phoneNumber,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = phoneNumber,
                        field = ItemDetailsFieldType.Plain.PhoneNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasBirthdate) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_personal_birthday_title),
                subtitle = birthdate,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = birthdate,
                        field = ItemDetailsFieldType.Plain.BirthDate
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
        title = stringResource(id = R.string.item_details_identity_section_personal_title),
        sections = sections.toPersistentList()
    )
}

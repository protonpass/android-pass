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
import proton.android.pass.domain.WorkDetailsContent

@Composable
internal fun PassIdentityItemDetailsWorkSection(
    modifier: Modifier = Modifier,
    workDetailsContent: WorkDetailsContent,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(workDetailsContent) {
    val sections = mutableListOf<@Composable () -> Unit>()

    if (hasCompany) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_work_company_title),
                subtitle = company,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = company,
                        field = ItemDetailsFieldType.Plain.Company
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasJobTitle) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_work_occupation_title),
                subtitle = jobTitle,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = jobTitle,
                        field = ItemDetailsFieldType.Plain.Occupation
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasWorkPhoneNumber) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_work_email_title),
                subtitle = workPhoneNumber,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = workPhoneNumber,
                        field = ItemDetailsFieldType.Plain.PhoneNumber
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasWorkEmail) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_work_phone_number_title),
                subtitle = workEmail,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = workEmail,
                        field = ItemDetailsFieldType.Plain.Email
                    ).also(onEvent)
                }
            )
        }
    }

    if (hasPersonalWebsite) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_work_personal_website_title),
                subtitle = personalWebsite,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = personalWebsite,
                        field = ItemDetailsFieldType.Plain.Website
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
        titleResId = R.string.item_details_identity_section_work_title,
        sections = sections.toPersistentList()
    )
}

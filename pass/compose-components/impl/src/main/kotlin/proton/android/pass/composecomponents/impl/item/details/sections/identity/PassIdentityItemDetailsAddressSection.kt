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
import proton.android.pass.domain.AddressDetailsContent

@Composable
internal fun PassIdentityItemDetailsAddressSection(
    modifier: Modifier = Modifier,
    addressDetailsContent: AddressDetailsContent,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(addressDetailsContent) {
    val sections = mutableListOf<@Composable () -> Unit>()

    if(hasStreetAddress) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_street_address_title),
                subtitle = streetAddress,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = streetAddress,
                        field = ItemDetailsFieldType.Plain.StreetAddress
                    ).also(onEvent)
                }
            )
        }
    }

    if(hasFloor) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_floor_title),
                subtitle = floor,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = floor,
                        field = ItemDetailsFieldType.Plain.Floor
                    ).also(onEvent)
                }
            )
        }
    }

    if(hasCity) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_city_title),
                subtitle = city,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = city,
                        field = ItemDetailsFieldType.Plain.City
                    ).also(onEvent)
                }
            )
        }
    }

    if(hasZipOrPostalCode) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_zip_or_postal_code_title),
                subtitle = zipOrPostalCode,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = zipOrPostalCode,
                        field = ItemDetailsFieldType.Plain.ZipOrPostalCode
                    ).also(onEvent)
                }
            )
        }
    }

    if(hasStateOrProvince) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_state_or_province_title),
                subtitle = stateOrProvince,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = stateOrProvince,
                        field = ItemDetailsFieldType.Plain.StateOrProvince
                    ).also(onEvent)
                }
            )
        }
    }

    if(hasCounty) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_county_title),
                subtitle = county,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = county,
                        field = ItemDetailsFieldType.Plain.County
                    ).also(onEvent)
                }
            )
        }
    }

    if(hasCountryOrRegion) {
        sections.add {
            PassItemDetailFieldRow(
                icon = null,
                title = stringResource(id = R.string.item_details_identity_section_address_country_or_region_title),
                subtitle = countryOrRegion,
                itemColors = itemColors,
                onClick = {
                    PassItemDetailsUiEvent.OnSectionClick(
                        section = countryOrRegion,
                        field = ItemDetailsFieldType.Plain.CountryOrRegion
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
        titleResId = R.string.item_details_identity_section_address_title,
        sections = sections.toPersistentList()
    )
}

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
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.addItemDetailsFieldRow
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.addCustomFieldRows
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.ItemDiffs

@Composable
internal fun PassIdentityItemDetailsAddressSection(
    modifier: Modifier = Modifier,
    addressDetailsContent: AddressDetailsContent,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Identity,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(addressDetailsContent) {
    val rows = mutableListOf<@Composable () -> Unit>()

    if (hasOrganization) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_organization_title,
            section = organization,
            field = ItemDetailsFieldType.Plain.Organization,
            itemColors = itemColors,
            itemDiffType = itemDiffs.organization,
            onEvent = onEvent
        )
    }

    if (hasStreetAddress) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_street_address_title,
            section = streetAddress,
            field = ItemDetailsFieldType.Plain.StreetAddress,
            itemColors = itemColors,
            itemDiffType = itemDiffs.streetAddress,
            onEvent = onEvent
        )
    }

    if (hasFloor) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_floor_title,
            section = floor,
            field = ItemDetailsFieldType.Plain.Floor,
            itemColors = itemColors,
            itemDiffType = itemDiffs.floor,
            onEvent = onEvent
        )
    }

    if (hasCity) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_city_title,
            section = city,
            field = ItemDetailsFieldType.Plain.City,
            itemColors = itemColors,
            itemDiffType = itemDiffs.city,
            onEvent = onEvent
        )
    }

    if (hasZipOrPostalCode) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_zip_or_postal_code_title,
            section = zipOrPostalCode,
            field = ItemDetailsFieldType.Plain.ZipOrPostalCode,
            itemColors = itemColors,
            itemDiffType = itemDiffs.zipOrPostalCode,
            onEvent = onEvent
        )
    }

    if (hasStateOrProvince) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_state_or_province_title,
            section = stateOrProvince,
            field = ItemDetailsFieldType.Plain.StateOrProvince,
            itemColors = itemColors,
            itemDiffType = itemDiffs.stateOrProvince,
            onEvent = onEvent
        )
    }

    if (hasCounty) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_county_title,
            section = county,
            field = ItemDetailsFieldType.Plain.County,
            itemColors = itemColors,
            itemDiffType = itemDiffs.county,
            onEvent = onEvent
        )
    }

    if (hasCountryOrRegion) {
        rows.addItemDetailsFieldRow(
            titleResId = R.string.item_details_identity_section_address_country_or_region_title,
            section = countryOrRegion,
            field = ItemDetailsFieldType.Plain.CountryOrRegion,
            itemColors = itemColors,
            itemDiffType = itemDiffs.countryOrRegion,
            onEvent = onEvent
        )
    }

    if (hasCustomFields) {
        rows.addCustomFieldRows(
            customFields = customFields,
            customFieldSection = ItemSection.Identity.Address,
            customFieldTotps = persistentMapOf(),
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent
        )
    }

    PassItemDetailsSection(
        modifier = modifier,
        title = stringResource(id = R.string.item_details_identity_section_address_title),
        sections = rows.toPersistentList()
    )
}

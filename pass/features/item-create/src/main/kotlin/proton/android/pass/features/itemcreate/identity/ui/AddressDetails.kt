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

package proton.android.pass.features.itemcreate.identity.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.features.itemcreate.common.customfields.AddMoreButton
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldEntry
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnCustomFieldOptions
import proton.android.pass.features.itemcreate.identity.navigation.IdentityContentEvent.OnFieldChange
import proton.android.pass.features.itemcreate.identity.presentation.FieldChange
import proton.android.pass.features.itemcreate.identity.presentation.UIAddressDetails
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.AddressCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.AddressDetailsField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.County
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Floor
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.FocusedField
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType.AddressDetails
import proton.android.pass.features.itemcreate.identity.ui.inputfields.CityInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.CountryOrRegionInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.CountyInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.FloorInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.OrganizationInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.StateOrProvinceInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.StreetAddressInput
import proton.android.pass.features.itemcreate.identity.ui.inputfields.ZipOrPostalCodeInput

@Composable
internal fun AddressDetails(
    modifier: Modifier = Modifier,
    uiAddressDetails: UIAddressDetails,
    enabled: Boolean,
    extraFields: PersistentSet<AddressDetailsField>,
    focusedField: Option<FocusedField>,
    showAddAddressDetailsButton: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    val field = focusedField.value()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            OrganizationInput(
                value = uiAddressDetails.organization,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.Organization(it))) }
            )
            PassDivider()
            StreetAddressInput(
                value = uiAddressDetails.streetAddress,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.StreetAddress(it))) }
            )
            PassDivider()
            ZipOrPostalCodeInput(
                value = uiAddressDetails.zipOrPostalCode,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.ZipOrPostalCode(it))) }
            )
            PassDivider()
            CityInput(
                value = uiAddressDetails.city,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.City(it))) }
            )
            PassDivider()
            StateOrProvinceInput(
                value = uiAddressDetails.stateOrProvince,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.StateOrProvince(it))) }
            )
            PassDivider()
            CountryOrRegionInput(
                value = uiAddressDetails.countryOrRegion,
                enabled = enabled,
                onChange = { onEvent(OnFieldChange(FieldChange.CountryOrRegion(it))) }
            )
            if (extraFields.contains(Floor)) {
                PassDivider()
                FloorInput(
                    value = uiAddressDetails.floor,
                    enabled = enabled,
                    requestFocus = field?.extraField is Floor,
                    onChange = { onEvent(OnFieldChange(FieldChange.Floor(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
            if (extraFields.contains(County)) {
                PassDivider()
                CountyInput(
                    value = uiAddressDetails.county,
                    enabled = enabled,
                    requestFocus = field?.extraField is County,
                    onChange = { onEvent(OnFieldChange(FieldChange.County(it))) },
                    onClearFocus = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
                )
            }
        }
        uiAddressDetails.customFields.forEachIndexed { index, value ->
            val focusRequester = remember { FocusRequester() }
            CustomFieldEntry(
                modifier = Modifier.focusRequester(focusRequester),
                entry = value,
                canEdit = enabled,
                isError = false,
                errorMessage = "",
                index = index,
                onValueChange = {
                    val fieldChange = FieldChange.CustomField(
                        sectionType = AddressDetails,
                        customFieldType = value.toCustomFieldType(),
                        index = index,
                        value = it
                    )
                    onEvent(OnFieldChange(fieldChange))
                },
                onFocusChange = { idx, isFocused ->
                    onEvent(IdentityContentEvent.OnCustomFieldFocused(idx, isFocused, AddressCustomField))
                },
                onOptionsClick = { onEvent(OnCustomFieldOptions(index, value.label, AddressCustomField)) }
            )
            RequestFocusLaunchedEffect(
                focusRequester = focusRequester,
                requestFocus = field?.extraField is AddressCustomField && field.index == index,
                callback = { onEvent(IdentityContentEvent.ClearLastAddedFieldFocus) }
            )

        }
        if (showAddAddressDetailsButton) {
            AddMoreButton(onClick = { onEvent(IdentityContentEvent.OnAddAddressDetailField) })
        }
    }
}

@Preview
@Composable
fun AddressDetailsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AddressDetails(
                uiAddressDetails = UIAddressDetails.EMPTY,
                enabled = true,
                extraFields = persistentSetOf(),
                focusedField = None,
                onEvent = {},
                showAddAddressDetailsButton = true
            )
        }
    }
}

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

package proton.android.pass.featureitemcreate.impl.identity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featureitemcreate.impl.identity.presentation.AddressDetails
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.CityInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.CountryOrRegionInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.OrganizationInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.StateOrProvinceInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.StreetAddressInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.ZipOrPostalCodeInput

@Composable
internal fun AddressDetails(
    modifier: Modifier = Modifier,
    addressDetails: AddressDetails,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        OrganizationInput(
            value = addressDetails.organization,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnFullNameChange(it)) }
        )
        PassDivider()
        StreetAddressInput(
            value = addressDetails.streetAddress,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        ZipOrPostalCodeInput(
            value = addressDetails.zipOrPostalCode,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        CityInput(
            value = addressDetails.city,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        StateOrProvinceInput(
            value = addressDetails.stateOrProvince,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
        PassDivider()
        CountryOrRegionInput(
            value = addressDetails.countryOrRegion,
            enabled = true,
            onChange = { onEvent(IdentityContentEvent.OnEmailChange(it)) }
        )
    }
}

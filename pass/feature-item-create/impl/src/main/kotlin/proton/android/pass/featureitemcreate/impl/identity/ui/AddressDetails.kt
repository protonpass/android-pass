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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.presentation.UIAddressDetails
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.CityInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.CountryOrRegionInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.OrganizationInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.StateOrProvinceInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.StreetAddressInput
import proton.android.pass.featureitemcreate.impl.identity.ui.inputfields.ZipOrPostalCodeInput

@Composable
internal fun AddressDetails(
    modifier: Modifier = Modifier,
    uiAddressDetails: UIAddressDetails,
    enabled: Boolean,
    onEvent: (IdentityContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            OrganizationInput(
                value = uiAddressDetails.organization,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnOrganizationChange(it)) }
            )
            PassDivider()
            StreetAddressInput(
                value = uiAddressDetails.streetAddress,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnStreetAddressChange(it)) }
            )
            PassDivider()
            ZipOrPostalCodeInput(
                value = uiAddressDetails.zipOrPostalCode,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnZipOrPostalCodeChange(it)) }
            )
            PassDivider()
            CityInput(
                value = uiAddressDetails.city,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnCityChange(it)) }
            )
            PassDivider()
            StateOrProvinceInput(
                value = uiAddressDetails.stateOrProvince,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnStateOrProvinceChange(it)) }
            )
            PassDivider()
            CountryOrRegionInput(
                value = uiAddressDetails.countryOrRegion,
                enabled = enabled,
                onChange = { onEvent(IdentityContentEvent.OnCountryOrRegionChange(it)) }
            )
        }
        AddMoreButton(onClick = { onEvent(IdentityContentEvent.OnAddAddressDetailField) })
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
                onEvent = {}
            )
        }
    }
}

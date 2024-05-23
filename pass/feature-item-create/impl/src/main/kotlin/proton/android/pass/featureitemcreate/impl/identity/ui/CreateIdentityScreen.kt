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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.identity.presentation.CreateProviderIdentityViewModel
import proton.android.pass.featureitemcreate.impl.identity.navigation.IdentityContentEvent
import proton.android.pass.featureitemcreate.impl.identity.navigation.BaseIdentityNavigation
import proton.android.pass.featureitemcreate.impl.login.PerformActionAfterKeyboardHide

@Composable
fun CreateIdentityScreen(
    modifier: Modifier = Modifier,
    selectVault: ShareId?,
    viewModel: CreateProviderIdentityViewModel = hiltViewModel(),
    onNavigate: (BaseIdentityNavigation) -> Unit
) {
    LaunchedEffect(selectVault) {
        if (selectVault != null) {
            viewModel.onVaultSelect(selectVault)
        }
    }

    var actionAfterKeyboardHide by remember { mutableStateOf<(() -> Unit)?>(null) }
    PerformActionAfterKeyboardHide(
        action = actionAfterKeyboardHide,
        clearAction = { actionAfterKeyboardHide = null }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    CreateIdentityContent(
        modifier = modifier.fillMaxSize(),
        identityItemFormState = viewModel.getFormState(),
        selectedVault = state.getSelectedVault()?.vault,
        shouldShowVaultSelector = state.shouldShowVaultSelector(),
        topBarActionName = stringResource(id = R.string.title_create),
        onEvent = { event ->
            when (event) {
                is IdentityContentEvent.OnVaultSelect -> viewModel.onVaultSelect(event.shareId)
                is IdentityContentEvent.Submit -> viewModel.onSubmit(event.shareId)
                IdentityContentEvent.Up ->
                    actionAfterKeyboardHide = { onNavigate(BaseIdentityNavigation.Close) }

                is IdentityContentEvent.OnTitleChange -> viewModel.onTitleChanged(event.title)
                is IdentityContentEvent.OnBirthdateChange -> viewModel.onBirthdateChanged(event.birthdate)
                is IdentityContentEvent.OnCityChange -> viewModel.onCityChanged(event.city)
                is IdentityContentEvent.OnCompanyChange -> viewModel.onCompanyChanged(event.company)
                is IdentityContentEvent.OnCountryOrRegionChange ->
                    viewModel.onCountryOrRegionChanged(event.countryOrRegion)

                is IdentityContentEvent.OnEmailChange -> viewModel.onEmailChanged(event.email)
                is IdentityContentEvent.OnFirstNameChange -> viewModel.onFirstNameChanged(event.firstName)
                is IdentityContentEvent.OnFullNameChange -> viewModel.onFullNameChanged(event.fullName)
                is IdentityContentEvent.OnGenderChange -> viewModel.onGenderChanged(event.gender)
                is IdentityContentEvent.OnJobTitleChange -> viewModel.onJobTitleChanged(event.jobTitle)
                is IdentityContentEvent.OnLastNameChange -> viewModel.onLastNameChanged(event.lastName)
                is IdentityContentEvent.OnLicenseNumberChange ->
                    viewModel.onLicenseNumberChanged(event.licenseNumber)

                is IdentityContentEvent.OnMiddleNameChange -> viewModel.onMiddleNameChanged(event.middleName)
                is IdentityContentEvent.OnOrganizationChange ->
                    viewModel.onOrganizationChanged(event.organization)

                is IdentityContentEvent.OnPassportNumberChange ->
                    viewModel.onPassportNumberChanged(event.passportNumber)

                is IdentityContentEvent.OnPhoneNumberChange -> viewModel.onPhoneNumberChanged(event.phoneNumber)
                is IdentityContentEvent.OnSocialSecurityNumberChange ->
                    viewModel.onSocialSecurityNumberChanged(event.socialSecurityNumber)

                is IdentityContentEvent.OnStateOrProvinceChange ->
                    viewModel.onStateOrProvinceChanged(event.stateOrProvince)

                is IdentityContentEvent.OnStreetAddressChange ->
                    viewModel.onStreetAddressChanged(event.streetAddress)

                is IdentityContentEvent.OnWebsiteChange -> viewModel.onWebsiteChanged(event.website)
                is IdentityContentEvent.OnXHandleChange -> viewModel.onXHandleChanged(event.xHandle)
                is IdentityContentEvent.OnZipOrPostalCodeChange ->
                    viewModel.onZipOrPostalCodeChanged(event.zipOrPostalCode)
            }
        }

    )
}

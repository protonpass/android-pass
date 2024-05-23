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

package proton.android.pass.featureitemcreate.impl.identity.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.scopes.ViewModelScoped
import proton.android.pass.commonui.api.SavedStateHandleProvider
import javax.inject.Inject

@ViewModelScoped
class IdentityActionsProviderImpl @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider
) : IdentityActionsProvider {

    @OptIn(SavedStateHandleSaveableApi::class)
    private var identityItemFormMutableState: IdentityItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(IdentityItemFormState.EMPTY) }

    override fun onTitleChanged(title: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(title = title)
    }

    // Personal Details
    override fun onFullNameChanged(fullName: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(fullName = fullName)
        )
    }

    override fun onFirstNameChanged(firstName: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(firstName = firstName)
        )
    }

    override fun onMiddleNameChanged(middleName: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(middleName = middleName)
        )
    }

    override fun onLastNameChanged(lastName: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(lastName = lastName)
        )
    }

    override fun onBirthdateChanged(birthdate: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(birthdate = birthdate)
        )
    }

    override fun onGenderChanged(gender: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(gender = gender)
        )
    }

    override fun onEmailChanged(email: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(email = email)
        )
    }

    override fun onPhoneNumberChanged(phoneNumber: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            personalDetails = identityItemFormMutableState.personalDetails.copy(phoneNumber = phoneNumber)
        )
    }

    // Address Details
    override fun onOrganizationChanged(organization: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            addressDetails = identityItemFormMutableState.addressDetails.copy(organization = organization)
        )
    }

    override fun onStreetAddressChanged(streetAddress: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            addressDetails = identityItemFormMutableState.addressDetails.copy(streetAddress = streetAddress)
        )
    }

    override fun onZipOrPostalCodeChanged(zipOrPostalCode: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            addressDetails = identityItemFormMutableState.addressDetails.copy(zipOrPostalCode = zipOrPostalCode)
        )
    }

    override fun onCityChanged(city: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            addressDetails = identityItemFormMutableState.addressDetails.copy(city = city)
        )
    }

    override fun onStateOrProvinceChanged(stateOrProvince: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            addressDetails = identityItemFormMutableState.addressDetails.copy(stateOrProvince = stateOrProvince)
        )
    }

    override fun onCountryOrRegionChanged(countryOrRegion: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            addressDetails = identityItemFormMutableState.addressDetails.copy(countryOrRegion = countryOrRegion)
        )
    }

    // Contact Details
    override fun onSocialSecurityNumberChanged(socialSecurityNumber: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            contactDetails = identityItemFormMutableState.contactDetails.copy(
                socialSecurityNumber = socialSecurityNumber
            )
        )
    }

    override fun onPassportNumberChanged(passportNumber: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            contactDetails = identityItemFormMutableState.contactDetails.copy(passportNumber = passportNumber)
        )
    }

    override fun onLicenseNumberChanged(licenseNumber: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            contactDetails = identityItemFormMutableState.contactDetails.copy(licenseNumber = licenseNumber)
        )
    }

    override fun onWebsiteChanged(website: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            contactDetails = identityItemFormMutableState.contactDetails.copy(website = website)
        )
    }

    override fun onXHandleChanged(xHandle: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            contactDetails = identityItemFormMutableState.contactDetails.copy(xHandle = xHandle)
        )
    }

    // Work Details
    override fun onCompanyChanged(company: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            workDetails = identityItemFormMutableState.workDetails.copy(company = company)
        )
    }

    override fun onJobTitleChanged(jobTitle: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            workDetails = identityItemFormMutableState.workDetails.copy(jobTitle = jobTitle)
        )
    }

    override fun getFormState(): IdentityItemFormState = identityItemFormMutableState
}

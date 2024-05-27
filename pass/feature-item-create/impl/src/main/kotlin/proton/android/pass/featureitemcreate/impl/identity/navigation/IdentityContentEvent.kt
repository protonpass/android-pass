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

package proton.android.pass.featureitemcreate.impl.identity.navigation

import proton.android.pass.domain.ShareId

sealed interface IdentityContentEvent {
    data object Up : IdentityContentEvent

    @JvmInline
    value class Submit(val shareId: ShareId) : IdentityContentEvent

    @JvmInline
    value class OnTitleChange(val title: String) : IdentityContentEvent

    @JvmInline
    value class OnFullNameChange(val fullName: String) : IdentityContentEvent

    @JvmInline
    value class OnFirstNameChange(val firstName: String) : IdentityContentEvent

    @JvmInline
    value class OnMiddleNameChange(val middleName: String) : IdentityContentEvent

    @JvmInline
    value class OnLastNameChange(val lastName: String) : IdentityContentEvent

    @JvmInline
    value class OnBirthdateChange(val birthdate: String) : IdentityContentEvent

    @JvmInline
    value class OnGenderChange(val gender: String) : IdentityContentEvent

    @JvmInline
    value class OnEmailChange(val email: String) : IdentityContentEvent

    @JvmInline
    value class OnPhoneNumberChange(val phoneNumber: String) : IdentityContentEvent

    @JvmInline
    value class OnOrganizationChange(val organization: String) : IdentityContentEvent

    @JvmInline
    value class OnStreetAddressChange(val streetAddress: String) : IdentityContentEvent

    @JvmInline
    value class OnZipOrPostalCodeChange(val zipOrPostalCode: String) : IdentityContentEvent

    @JvmInline
    value class OnCityChange(val city: String) : IdentityContentEvent

    @JvmInline
    value class OnStateOrProvinceChange(val stateOrProvince: String) : IdentityContentEvent

    @JvmInline
    value class OnCountryOrRegionChange(val countryOrRegion: String) : IdentityContentEvent

    @JvmInline
    value class OnSocialSecurityNumberChange(val socialSecurityNumber: String) :
        IdentityContentEvent

    @JvmInline
    value class OnPassportNumberChange(val passportNumber: String) : IdentityContentEvent

    @JvmInline
    value class OnLicenseNumberChange(val licenseNumber: String) : IdentityContentEvent

    @JvmInline
    value class OnWebsiteChange(val website: String) : IdentityContentEvent

    @JvmInline
    value class OnXHandleChange(val xHandle: String) : IdentityContentEvent

    @JvmInline
    value class OnSecondPhoneNumberChange(val phoneNumber: String) : IdentityContentEvent

    @JvmInline
    value class OnCompanyChange(val company: String) : IdentityContentEvent

    @JvmInline
    value class OnJobTitleChange(val jobTitle: String) : IdentityContentEvent

    @JvmInline
    value class OnVaultSelect(val shareId: ShareId) : IdentityContentEvent
}

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

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.Item
import proton.android.pass.featureitemcreate.impl.ItemSavedState

@Suppress("ComplexInterface", "TooManyFunctions")
interface IdentityFormActions {
    fun onTitleChanged(title: String)
    fun onFullNameChanged(fullName: String)
    fun onFirstNameChanged(firstName: String)
    fun onMiddleNameChanged(middleName: String)
    fun onLastNameChanged(lastName: String)
    fun onBirthdateChanged(birthdate: String)
    fun onGenderChanged(gender: String)
    fun onEmailChanged(email: String)
    fun onPhoneNumberChanged(phoneNumber: String)
    fun onOrganizationChanged(organization: String)
    fun onStreetAddressChanged(streetAddress: String)
    fun onZipOrPostalCodeChanged(zipOrPostalCode: String)
    fun onCityChanged(city: String)
    fun onStateOrProvinceChanged(stateOrProvince: String)
    fun onCountryOrRegionChanged(countryOrRegion: String)
    fun onSocialSecurityNumberChanged(socialSecurityNumber: String)
    fun onPassportNumberChanged(passportNumber: String)
    fun onLicenseNumberChanged(licenseNumber: String)
    fun onWebsiteChanged(website: String)
    fun onXHandleChanged(xHandle: String)
    fun onSecondPhoneNumberChanged(phoneNumber: String)
    fun onCompanyChanged(company: String)
    fun onJobTitleChanged(jobTitle: String)
    fun getFormState(): IdentityItemFormState
    fun isFormStateValid(): Boolean
}

interface IdentityActionsProvider : IdentityFormActions {
    fun observeSharedState(): Flow<IdentitySharedUiState>
    fun updateLoadingState(loadingState: IsLoadingState)
    fun onItemSavedState(item: Item)
}

data class IdentitySharedUiState(
    val isLoadingState: IsLoadingState,
    val hasUserEditedContent: Boolean,
    val validationErrors: PersistentSet<IdentityValidationErrors>,
    val isItemSaved: ItemSavedState
) {
    companion object {
        val Initial = IdentitySharedUiState(
            isLoadingState = IsLoadingState.NotLoading,
            hasUserEditedContent = false,
            validationErrors = persistentSetOf(),
            isItemSaved = ItemSavedState.Unknown
        )
    }
}

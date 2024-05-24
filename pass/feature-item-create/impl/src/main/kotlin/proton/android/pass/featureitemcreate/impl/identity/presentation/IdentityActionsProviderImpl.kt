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
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import javax.inject.Inject

@Suppress("TooManyFunctions")
@ViewModelScoped
class IdentityActionsProviderImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : IdentityActionsProvider {

    @OptIn(SavedStateHandleSaveableApi::class)
    private var identityItemFormMutableState: IdentityItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(IdentityItemFormState.EMPTY) }
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val validationErrorsState: MutableStateFlow<Set<IdentityValidationErrors>> =
        MutableStateFlow(emptySet())
    private val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    private val state: Flow<IdentitySharedUiState> = combine(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState.map { it.toPersistentSet() },
        isItemSavedState,
        ::IdentitySharedUiState
    )

    override fun onTitleChanged(title: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(title = title)
    }

    // Personal Details
    override fun onFullNameChanged(fullName: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(fullName = fullName)
        )
    }

    override fun onFirstNameChanged(firstName: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(firstName = firstName)
        )
    }

    override fun onMiddleNameChanged(middleName: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(middleName = middleName)
        )
    }

    override fun onLastNameChanged(lastName: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(lastName = lastName)
        )
    }

    override fun onBirthdateChanged(birthdate: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(birthdate = birthdate)
        )
    }

    override fun onGenderChanged(gender: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(gender = gender)
        )
    }

    override fun onEmailChanged(email: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(email = email)
        )
    }

    override fun onPhoneNumberChanged(phoneNumber: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(phoneNumber = phoneNumber)
        )
    }

    // Address Details
    override fun onOrganizationChanged(organization: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(organization = organization)
        )
    }

    override fun onStreetAddressChanged(streetAddress: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(streetAddress = streetAddress)
        )
    }

    override fun onZipOrPostalCodeChanged(zipOrPostalCode: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(zipOrPostalCode = zipOrPostalCode)
        )
    }

    override fun onCityChanged(city: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(city = city)
        )
    }

    override fun onStateOrProvinceChanged(stateOrProvince: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(stateOrProvince = stateOrProvince)
        )
    }

    override fun onCountryOrRegionChanged(countryOrRegion: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(countryOrRegion = countryOrRegion)
        )
    }

    // Contact Details
    override fun onSocialSecurityNumberChanged(socialSecurityNumber: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                socialSecurityNumber = socialSecurityNumber
            )
        )
    }

    override fun onPassportNumberChanged(passportNumber: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(passportNumber = passportNumber)
        )
    }

    override fun onLicenseNumberChanged(licenseNumber: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(licenseNumber = licenseNumber)
        )
    }

    override fun onWebsiteChanged(website: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(website = website)
        )
    }

    override fun onXHandleChanged(xHandle: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(xHandle = xHandle)
        )
    }

    // Work Details
    override fun onCompanyChanged(company: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(company = company)
        )
    }

    override fun onJobTitleChanged(jobTitle: String) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(jobTitle = jobTitle)
        )
    }

    override fun getFormState(): IdentityItemFormState = identityItemFormMutableState

    override fun isFormStateValid(): Boolean {
        val validationErrors = identityItemFormMutableState.validate()
        if (validationErrors.isNotEmpty()) {
            validationErrorsState.update { validationErrors }
            return false
        }
        return true
    }

    override fun observeSharedState(): Flow<IdentitySharedUiState> = state

    override fun updateLoadingState(loadingState: IsLoadingState) {
        isLoadingState.update { loadingState }
    }

    override fun onItemSavedState(item: Item) {
        val itemSavedState = encryptionContextProvider.withEncryptionContext {
            ItemSavedState.Success(
                item.id,
                item.toUiModel(this@withEncryptionContext)
            )
        }
        isItemSavedState.update { itemSavedState }
    }
}

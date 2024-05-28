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
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.IdentityFieldDraftRepository
import javax.inject.Inject

@Suppress("TooManyFunctions")
@ViewModelScoped
class IdentityActionsProviderImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val identityFieldDraftRepository: IdentityFieldDraftRepository,
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
        identityFieldDraftRepository.observeExtraFields().map { it.toPersistentSet() },
        ::IdentitySharedUiState
    )

    @Suppress("LongMethod")
    override fun onFieldChange(field: FieldChange) {
        hasUserEditedContentState.update { true }
        identityItemFormMutableState = when (field) {
            is FieldChange.Birthdate -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(birthdate = field.value)
            )
            is FieldChange.City -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(city = field.value)
            )
            is FieldChange.Company -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(company = field.value)
            )
            is FieldChange.CountryOrRegion -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(countryOrRegion = field.value)
            )
            is FieldChange.County -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(county = field.value)
            )
            is FieldChange.Email -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(email = field.value)
            )
            is FieldChange.Facebook -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(facebook = field.value)
            )
            is FieldChange.FirstName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(firstName = field.value)
            )
            is FieldChange.Floor -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(floor = field.value)
            )
            is FieldChange.FullName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(fullName = field.value)
            )
            is FieldChange.Gender -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(gender = field.value)
            )
            is FieldChange.Instagram -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(instagram = field.value)
            )
            is FieldChange.JobTitle -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(jobTitle = field.value)
            )
            is FieldChange.LastName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(lastName = field.value)
            )
            is FieldChange.LicenseNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(licenseNumber = field.value)
            )
            is FieldChange.Linkedin -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(linkedin = field.value)
            )
            is FieldChange.MiddleName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(middleName = field.value)
            )
            is FieldChange.Organization -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(organization = field.value)
            )
            is FieldChange.PassportNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(passportNumber = field.value)
            )
            is FieldChange.PersonalWebsite -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(personalWebsite = field.value)
            )
            is FieldChange.PhoneNumber -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(phoneNumber = field.value)
            )
            is FieldChange.Reddit -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(reddit = field.value)
            )
            is FieldChange.SecondPhoneNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(secondPhoneNumber = field.value)
            )
            is FieldChange.SocialSecurityNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    socialSecurityNumber = field.value
                )
            )
            is FieldChange.StateOrProvince -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(stateOrProvince = field.value)
            )
            is FieldChange.StreetAddress -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(streetAddress = field.value)
            )
            is FieldChange.Title -> identityItemFormMutableState.copy(title = field.value)
            is FieldChange.Website -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(website = field.value)
            )
            is FieldChange.WorkEmail -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(workEmail = field.value)
            )
            is FieldChange.WorkPhoneNumber -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(workPhoneNumber = field.value)
            )
            is FieldChange.XHandle -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(xHandle = field.value)
            )
            is FieldChange.Yahoo -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(yahoo = field.value)
            )
            is FieldChange.ZipOrPostalCode -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(zipOrPostalCode = field.value)
            )
        }
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

    override fun clearState() {
        identityFieldDraftRepository.clearAddedFields()
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

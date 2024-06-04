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
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.Item
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.CustomFieldIndexTitle
import proton.android.pass.featureitemcreate.impl.common.UICustomFieldContent
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.AddressCustomField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ContactCustomField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.CustomExtraField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.ExtraSectionCustomField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.IdentityFieldDraftRepository
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.PersonalCustomField
import proton.android.pass.featureitemcreate.impl.identity.presentation.bottomsheets.WorkCustomField
import proton.android.pass.featureitemcreate.impl.identity.ui.IdentitySectionType
import javax.inject.Inject

@Suppress("TooManyFunctions")
@ViewModelScoped
class IdentityActionsProviderImpl @Inject constructor(
    private val draftRepository: DraftRepository,
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
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    countryOrRegion = field.value
                )
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
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    secondPhoneNumber = field.value
                )
            )

            is FieldChange.SocialSecurityNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    socialSecurityNumber = field.value
                )
            )

            is FieldChange.StateOrProvince -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    stateOrProvince = field.value
                )
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
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    zipOrPostalCode = field.value
                )
            )

            is FieldChange.CustomField -> updateCustomFieldState(field, encryptionContextProvider)
        }
    }

    override fun onAddExtraSection(value: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections +
                listOf(UIExtraSection(value, emptyList()))
        )
    }

    override fun onRenameCustomSection(value: CustomFieldIndexTitle) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                .apply {
                    set(
                        value.index,
                        identityItemFormMutableState.uiExtraSections[value.index].copy(title = value.title)
                    )
                }
        )
    }

    override fun onRemoveCustomSection(index: Int) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                .apply { removeAt(index) }
        )
    }

    override fun onAddCustomField(value: CustomFieldContent, customExtraField: CustomExtraField) {
        val uiCustomFieldContent = UICustomFieldContent.from(value)
        identityItemFormMutableState = when (customExtraField) {
            AddressCustomField -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields +
                        listOf(uiCustomFieldContent)
                )
            )

            ContactCustomField -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields +
                        listOf(uiCustomFieldContent)
                )
            )

            PersonalCustomField -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields +
                        listOf(uiCustomFieldContent)
                )
            )

            WorkCustomField -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields +
                        listOf(uiCustomFieldContent)
                )
            )

            is ExtraSectionCustomField -> identityItemFormMutableState.copy(
                uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                    .apply {
                        set(
                            customExtraField.index,
                            identityItemFormMutableState.uiExtraSections[customExtraField.index].copy(
                                customFields = identityItemFormMutableState.uiExtraSections[customExtraField.index]
                                    .customFields + listOf(uiCustomFieldContent)
                            )
                        )
                    }
            )

        }
    }

    override fun onRemoveCustomField(index: Int, customExtraField: CustomExtraField) {
        identityItemFormMutableState = when (customExtraField) {
            AddressCustomField -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            ContactCustomField -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            PersonalCustomField -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            WorkCustomField -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is ExtraSectionCustomField -> identityItemFormMutableState.copy(
                uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                    .apply {
                        set(
                            customExtraField.index,
                            identityItemFormMutableState.uiExtraSections[customExtraField.index].copy(
                                customFields = identityItemFormMutableState.uiExtraSections[customExtraField.index]
                                    .customFields
                                    .toMutableList()
                                    .apply { removeAt(index) }
                            )
                        )
                    }
            )
        }
    }

    @Suppress("LongMethod")
    override fun onRenameCustomField(value: CustomFieldIndexTitle, customExtraField: CustomExtraField) {
        val (content, index) = when (customExtraField) {
            AddressCustomField ->
                identityItemFormMutableState.uiAddressDetails.customFields[value.index] to value.index
            ContactCustomField ->
                identityItemFormMutableState.uiContactDetails.customFields[value.index] to value.index
            PersonalCustomField ->
                identityItemFormMutableState.uiPersonalDetails.customFields[value.index] to value.index
            WorkCustomField ->
                identityItemFormMutableState.uiWorkDetails.customFields[value.index] to value.index
            is ExtraSectionCustomField ->
                identityItemFormMutableState.uiExtraSections[customExtraField.index]
                    .customFields[value.index] to value.index
        }
        val updated = when (content) {
            is UICustomFieldContent.Hidden -> {
                UICustomFieldContent.Hidden(
                    label = value.title,
                    value = content.value
                )
            }

            is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                label = value.title,
                value = content.value
            )

            is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                label = value.title,
                value = content.value,
                id = content.id
            )
        }
        identityItemFormMutableState = when (customExtraField) {
            AddressCustomField -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            ContactCustomField -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            PersonalCustomField -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            WorkCustomField -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            is ExtraSectionCustomField -> identityItemFormMutableState.copy(
                uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                    .apply {
                        set(
                            customExtraField.index,
                            identityItemFormMutableState.uiExtraSections[index].copy(
                                customFields = identityItemFormMutableState.uiExtraSections[index]
                                    .customFields
                                    .toMutableList()
                                    .apply { set(index, updated) }
                            )
                        )
                    }
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

    override fun updateSelectedSection(customExtraField: CustomExtraField) {
        draftRepository.save(DRAFT_IDENTITY_CUSTOM_FIELD_KEY, customExtraField)
    }

    @Suppress("LongMethod")
    private fun updateCustomFieldState(
        field: FieldChange.CustomField,
        encryptionContextProvider: EncryptionContextProvider
    ): IdentityItemFormState {
        val (content, index) = when (field.sectionType) {
            IdentitySectionType.PersonalDetails ->
                identityItemFormMutableState.uiPersonalDetails.customFields[field.index] to field.index

            IdentitySectionType.ContactDetails ->
                identityItemFormMutableState.uiContactDetails.customFields[field.index] to field.index

            IdentitySectionType.AddressDetails ->
                identityItemFormMutableState.uiAddressDetails.customFields[field.index] to field.index

            IdentitySectionType.WorkDetails ->
                identityItemFormMutableState.uiWorkDetails.customFields[field.index] to field.index

            is IdentitySectionType.ExtraSection ->
                identityItemFormMutableState.uiExtraSections[field.sectionType.index]
                    .customFields[field.index] to field.index
        }

        val updated = encryptionContextProvider.withEncryptionContext {
            when (content) {
                is UICustomFieldContent.Hidden -> {
                    UICustomFieldContent.Hidden(
                        label = content.label,
                        value = UIHiddenState.Revealed(
                            encrypted = encrypt(field.value),
                            clearText = field.value
                        )
                    )
                }

                is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                    label = content.label,
                    value = field.value
                )

                is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                    label = content.label,
                    value = UIHiddenState.Revealed(
                        encrypted = encrypt(field.value),
                        clearText = field.value
                    ),
                    id = content.id
                )
            }
        }
        return when (field.sectionType) {
            IdentitySectionType.PersonalDetails -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            IdentitySectionType.ContactDetails -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            IdentitySectionType.AddressDetails -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            IdentitySectionType.WorkDetails -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            is IdentitySectionType.ExtraSection -> identityItemFormMutableState.copy(
                uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                    .apply {
                        set(
                            field.sectionType.index,
                            identityItemFormMutableState.uiExtraSections[field.sectionType.index].copy(
                                customFields = identityItemFormMutableState.uiExtraSections[field.sectionType.index]
                                    .customFields
                                    .toMutableList()
                                    .apply { set(index, updated) }
                            )
                        )
                    }
            )
        }
    }
}

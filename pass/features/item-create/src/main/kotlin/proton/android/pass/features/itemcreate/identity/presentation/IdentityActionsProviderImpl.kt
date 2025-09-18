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

package proton.android.pass.features.itemcreate.identity.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.DraftFormSectionEvent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent.Companion.createCustomField
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.common.formprocessor.FormProcessingResult
import proton.android.pass.features.itemcreate.common.formprocessor.IdentityItemFormProcessor
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemRenameAttachmentsError
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.IdentityFieldDraftRepository
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.net.URI
import javax.inject.Inject

@Suppress("TooManyFunctions", "LargeClass", "LongParameterList")
@ViewModelScoped
class IdentityActionsProviderImpl @Inject constructor(
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val identityFieldDraftRepository: IdentityFieldDraftRepository,
    private val customFieldDraftRepository: CustomFieldDraftRepository,
    private val canPerformPaidAction: CanPerformPaidAction,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val renameAttachments: RenameAttachments,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val identityItemFormProcessor: IdentityItemFormProcessor,
    private val customFieldHandler: CustomFieldHandler,
    private val appDispatchers: AppDispatchers,
    private val clipboardManager: ClipboardManager,
    savedStateHandleProvider: SavedStateHandleProvider
) : IdentityActionsProvider {

    private var itemState: MutableStateFlow<Option<Item>> = MutableStateFlow(None)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var identityItemFormMutableState: IdentityItemFormState by savedStateHandleProvider.get()
        .saveable {
            encryptionContextProvider.withEncryptionContext {
                mutableStateOf(IdentityItemFormState.default(this@withEncryptionContext))
            }
        }
    private val identityItemFormState: IdentityItemFormState get() = identityItemFormMutableState

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val validationErrorsState: MutableStateFlow<Set<ValidationError>> =
        MutableStateFlow(emptySet())
    private val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    private val focusedFieldState: MutableStateFlow<Option<IdentityField>> = MutableStateFlow(None)

    @Suppress("LongMethod")
    override fun onFieldChange(field: IdentityField, value: String) {
        onUserEditedContent()
        identityItemFormMutableState = when (field) {
            is IdentityField.Birthdate -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(birthdate = value)
            )

            is IdentityField.City -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(city = value)
            )

            is IdentityField.Company -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(company = value)
            )

            is IdentityField.CountryOrRegion -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    countryOrRegion = value
                )
            )

            is IdentityField.County -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(county = value)
            )

            is IdentityField.Email -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(email = value)
            )

            is IdentityField.Facebook -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(facebook = value)
            )

            is IdentityField.FirstName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(firstName = value)
            )

            is IdentityField.Floor -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(floor = value)
            )

            is IdentityField.FullName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(fullName = value)
            )

            is IdentityField.Gender -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(gender = value)
            )

            is IdentityField.Instagram -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(instagram = value)
            )

            is IdentityField.JobTitle -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(jobTitle = value)
            )

            is IdentityField.LastName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(lastName = value)
            )

            is IdentityField.LicenseNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(licenseNumber = value)
            )

            is IdentityField.Linkedin -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(linkedin = value)
            )

            is IdentityField.MiddleName -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(middleName = value)
            )

            is IdentityField.Organization -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(organization = value)
            )

            is IdentityField.PassportNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(passportNumber = value)
            )

            is IdentityField.PersonalWebsite -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(personalWebsite = value)
            )

            is IdentityField.PhoneNumber -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(phoneNumber = value)
            )

            is IdentityField.Reddit -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(reddit = value)
            )

            is IdentityField.SecondPhoneNumber -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    secondPhoneNumber = value
                )
            )

            is IdentityField.SocialSecurityNumber -> encryptionContextProvider.withEncryptionContext {
                value
                    .let { socialSecurityNumber ->
                        if (socialSecurityNumber.isBlank()) {
                            UIHiddenState.Empty(encrypt(socialSecurityNumber))
                        } else {
                            UIHiddenState.Revealed(
                                encrypt(socialSecurityNumber),
                                socialSecurityNumber
                            )
                        }
                    }
                    .let { socialSecurityNumberUiHiddenState ->
                        identityItemFormMutableState.copy(
                            uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                                socialSecurityNumber = socialSecurityNumberUiHiddenState
                            )
                        )
                    }
            }

            is IdentityField.StateOrProvince -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    stateOrProvince = value
                )
            )

            is IdentityField.StreetAddress -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(streetAddress = value)
            )

            is IdentityField.Title -> identityItemFormMutableState.copy(title = value)
            is IdentityField.Website -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(website = value)
            )

            is IdentityField.WorkEmail -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(workEmail = value)
            )

            is IdentityField.WorkPhoneNumber -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(workPhoneNumber = value)
            )

            is IdentityField.XHandle -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(xHandle = value)
            )

            is IdentityField.Yahoo -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(yahoo = value)
            )

            is IdentityField.ZipOrPostalCode -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    zipOrPostalCode = value
                )
            )

            is IdentityField.CustomField -> updateCustomFieldState(field, value)
        }
    }

    private fun onAddExtraSection(event: DraftFormSectionEvent.SectionAdded) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections +
                listOf(UIExtraSection(event.label, emptyList()))
        )
    }

    private fun onRenameCustomSection(event: DraftFormSectionEvent.SectionRenamed) {
        val updatedSection = identityItemFormMutableState.uiExtraSections[event.index]
            .copy(title = event.newLabel)
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                .apply { set(event.index, updatedSection) }
        )
    }

    private fun onRemoveCustomSection(event: DraftFormSectionEvent.SectionRemoved) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                .apply { removeAt(event.index) }
        )
    }

    @Suppress("LongMethod")
    private fun onAddCustomField(event: DraftFormFieldEvent.FieldAdded) {
        val (_, label, type) = event
        val customField = draftRepository
            .delete<IdentityField.CustomField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
            .value()
            ?: return
        val field = encryptionContextProvider.withEncryptionContext {
            createCustomField(type, label, this)
        }
        identityItemFormMutableState = when (customField.sectionType) {
            is IdentitySectionType.AddressDetails -> {
                val addressDetails = identityItemFormState.uiAddressDetails
                identityItemFormState.copy(
                    uiAddressDetails = addressDetails.copy(
                        customFields = addressDetails.customFields + field
                    )
                )
            }

            is IdentitySectionType.ContactDetails -> {
                val contactDetails = identityItemFormState.uiContactDetails
                identityItemFormState.copy(
                    uiContactDetails = contactDetails.copy(
                        customFields = contactDetails.customFields + field
                    )
                )
            }

            is IdentitySectionType.PersonalDetails -> {
                val personalDetails = identityItemFormState.uiPersonalDetails
                identityItemFormState.copy(
                    uiPersonalDetails = personalDetails.copy(
                        customFields = personalDetails.customFields + field
                    )
                )
            }

            is IdentitySectionType.WorkDetails -> {
                val workDetails = identityItemFormState.uiWorkDetails
                identityItemFormState.copy(
                    uiWorkDetails = workDetails.copy(
                        customFields = workDetails.customFields + field
                    )
                )
            }

            is IdentitySectionType.ExtraSection -> {
                val extraSection = identityItemFormState.uiExtraSections
                identityItemFormState.copy(
                    uiExtraSections = extraSection.toMutableList()
                        .apply {
                            val section = extraSection[customField.sectionType.index]
                            set(
                                customField.sectionType.index,
                                section.copy(customFields = section.customFields + field)
                            )
                        }
                )
            }
        }
        val focusedIndex = when (customField.sectionType) {
            IdentitySectionType.AddressDetails ->
                identityItemFormState.uiAddressDetails.customFields.lastIndex
            IdentitySectionType.ContactDetails ->
                identityItemFormState.uiContactDetails.customFields.lastIndex
            IdentitySectionType.PersonalDetails ->
                identityItemFormState.uiPersonalDetails.customFields.lastIndex
            IdentitySectionType.WorkDetails ->
                identityItemFormState.uiWorkDetails.customFields.lastIndex
            is IdentitySectionType.ExtraSection ->
                identityItemFormState.uiExtraSections[customField.sectionType.index].customFields.lastIndex
        }
        focusedFieldState.update {
            IdentityField.CustomField(customField.sectionType, type, focusedIndex).some()
        }
    }

    private fun onRemoveCustomField(event: DraftFormFieldEvent.FieldRemoved) {
        val (_, index) = event
        val customField = draftRepository
            .delete<IdentityField.CustomField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
            .value()
            ?: return
        identityItemFormMutableState = when (customField.sectionType) {
            is IdentitySectionType.AddressDetails -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is IdentitySectionType.ContactDetails -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is IdentitySectionType.PersonalDetails -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is IdentitySectionType.WorkDetails -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is IdentitySectionType.ExtraSection -> identityItemFormMutableState.copy(
                uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                    .apply {
                        val sectionIndex = customField.sectionType.index
                        val section = identityItemFormMutableState.uiExtraSections[sectionIndex]
                        set(
                            sectionIndex,
                            section.copy(
                                customFields = section.customFields.toMutableList()
                                    .apply { removeAt(index) }
                            )
                        )
                    }
            )
        }
    }

    @Suppress("LongMethod")
    private fun onRenameCustomField(event: DraftFormFieldEvent.FieldRenamed) {
        val (_, customFieldIndex, newLabel) = event
        val customField = draftRepository
            .delete<IdentityField.CustomField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
            .value()
            ?: return
        val customFieldContent = when (customField.sectionType) {
            is IdentitySectionType.AddressDetails ->
                identityItemFormMutableState.uiAddressDetails.customFields[customFieldIndex]

            is IdentitySectionType.ContactDetails ->
                identityItemFormMutableState.uiContactDetails.customFields[customFieldIndex]

            is IdentitySectionType.PersonalDetails ->
                identityItemFormMutableState.uiPersonalDetails.customFields[customFieldIndex]

            is IdentitySectionType.WorkDetails ->
                identityItemFormMutableState.uiWorkDetails.customFields[customFieldIndex]

            is IdentitySectionType.ExtraSection ->
                identityItemFormMutableState.uiExtraSections[customField.sectionType.index]
                    .customFields[customFieldIndex]
        }
        val updatedCustomField = when (customFieldContent) {
            is UICustomFieldContent.Hidden -> {
                UICustomFieldContent.Hidden(
                    label = newLabel,
                    value = customFieldContent.value
                )
            }

            is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                label = newLabel,
                value = customFieldContent.value
            )

            is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                label = newLabel,
                value = customFieldContent.value,
                id = customFieldContent.id
            )

            is UICustomFieldContent.Date -> UICustomFieldContent.Date(
                label = newLabel,
                value = customFieldContent.value
            )
        }
        identityItemFormMutableState = when (customField.sectionType) {
            is IdentitySectionType.AddressDetails -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { set(customFieldIndex, updatedCustomField) }
                )
            )

            is IdentitySectionType.ContactDetails -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { set(customFieldIndex, updatedCustomField) }
                )
            )

            is IdentitySectionType.PersonalDetails -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { set(customFieldIndex, updatedCustomField) }
                )
            )

            is IdentitySectionType.WorkDetails -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields.toMutableList()
                        .apply { set(customFieldIndex, updatedCustomField) }
                )
            )

            is IdentitySectionType.ExtraSection -> {
                val extraSections = identityItemFormMutableState.uiExtraSections
                val sectionToUpdate = extraSections[customField.sectionType.index]
                val newSectionContent = sectionToUpdate.copy(
                    customFields = sectionToUpdate
                        .customFields
                        .toMutableList()
                        .apply { set(customFieldIndex, updatedCustomField) }
                )
                val updatedExtraSections = extraSections.toMutableList()
                    .apply { set(customField.sectionType.index, newSectionContent) }

                identityItemFormMutableState.copy(uiExtraSections = updatedExtraSections)
            }
        }
    }

    override fun getFormState(): IdentityItemFormState = identityItemFormMutableState

    override suspend fun isFormStateValid(
        originalPersonalCustomFields: List<UICustomFieldContent>,
        originalAddressCustomFields: List<UICustomFieldContent>,
        originalContactCustomFields: List<UICustomFieldContent>,
        originalWorkCustomFields: List<UICustomFieldContent>,
        originalSections: List<UIExtraSection>
    ): Boolean {
        val result = encryptionContextProvider.withEncryptionContextSuspendable {
            identityItemFormProcessor.process(
                IdentityItemFormProcessor.Input(
                    formState = getFormState(),
                    originalPersonalCustomFields = originalPersonalCustomFields,
                    originalAddressCustomFields = originalAddressCustomFields,
                    originalContactCustomFields = originalContactCustomFields,
                    originalWorkCustomFields = originalWorkCustomFields,
                    originalSections = originalSections
                ),
                ::decrypt,
                ::encrypt
            )
        }
        return when (result) {
            is FormProcessingResult.Error -> {
                validationErrorsState.update { result.errors }
                false
            }

            is FormProcessingResult.Success -> {
                identityItemFormMutableState = result.sanitized
                true
            }
        }
    }

    override fun clearDraftData() {
        identityFieldDraftRepository.clearAddedFields()
        attachmentsHandler.onClearAttachments()
    }

    @Suppress("LongMethod")
    override fun onFocusChange(field: IdentityField, isFocused: Boolean) {
        when (field) {
            is IdentityField.CustomField -> when (field.sectionType) {
                IdentitySectionType.AddressDetails ->
                    identityItemFormMutableState = identityItemFormState.copy(
                        uiAddressDetails = identityItemFormState.uiAddressDetails.copy(
                            customFields = customFieldHandler.onCustomFieldFocusedChanged(
                                customFieldIdentifier = CustomFieldIdentifier(
                                    index = field.index,
                                    type = field.customFieldType
                                ),
                                customFieldList = identityItemFormState.uiAddressDetails.customFields,
                                isFocused = isFocused
                            )
                        )
                    )

                IdentitySectionType.ContactDetails ->
                    identityItemFormMutableState = identityItemFormState.copy(
                        uiContactDetails = identityItemFormState.uiContactDetails.copy(
                            customFields = customFieldHandler.onCustomFieldFocusedChanged(
                                customFieldIdentifier = CustomFieldIdentifier(
                                    index = field.index,
                                    type = field.customFieldType
                                ),
                                customFieldList = identityItemFormState.uiContactDetails.customFields,
                                isFocused = isFocused
                            )
                        )
                    )

                IdentitySectionType.PersonalDetails ->
                    identityItemFormMutableState = identityItemFormState.copy(
                        uiPersonalDetails = identityItemFormState.uiPersonalDetails.copy(
                            customFields = customFieldHandler.onCustomFieldFocusedChanged(
                                customFieldIdentifier = CustomFieldIdentifier(
                                    index = field.index,
                                    type = field.customFieldType
                                ),
                                customFieldList = identityItemFormState.uiPersonalDetails.customFields,
                                isFocused = isFocused
                            )
                        )
                    )

                IdentitySectionType.WorkDetails ->
                    identityItemFormMutableState = identityItemFormState.copy(
                        uiWorkDetails = identityItemFormState.uiWorkDetails.copy(
                            customFields = customFieldHandler.onCustomFieldFocusedChanged(
                                customFieldIdentifier = CustomFieldIdentifier(
                                    index = field.index,
                                    type = field.customFieldType
                                ),
                                customFieldList = identityItemFormState.uiWorkDetails.customFields,
                                isFocused = isFocused
                            )
                        )
                    )

                is IdentitySectionType.ExtraSection -> {
                    val id = CustomFieldIdentifier(
                        sectionIndex = field.sectionType.index.some(),
                        index = field.index,
                        type = field.customFieldType
                    )
                    val section = identityItemFormState.uiExtraSections[field.sectionType.index]
                    val updatedSection: UIExtraSection =
                        section.copy(
                            customFields = customFieldHandler.onCustomFieldFocusedChanged(
                                customFieldIdentifier = id,
                                customFieldList = section.customFields,
                                isFocused = isFocused
                            )
                        )
                    identityItemFormMutableState = identityItemFormState.copy(
                        uiExtraSections = identityItemFormState.uiExtraSections.toMutableList()
                            .apply {
                                set(field.sectionType.index, updatedSection)
                            }
                    )
                }
            }

            IdentityField.SocialSecurityNumber ->
                onSocialSecurityNumberFieldFocusChange(isFocused)

            else -> {}
        }

        if (isFocused) {
            focusedFieldState.update { field.some() }
        } else {
            focusedFieldState.update { None }
        }
    }

    override fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(contextHolder, uri, mimetype)
    }

    override fun observeSharedState(): Flow<IdentitySharedUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState.map { it.toPersistentSet() },
        isItemSavedState,
        identityFieldDraftRepository.observeExtraFields().map(Set<IdentityField>::toPersistentSet),
        focusedFieldState,
        canPerformPaidAction(),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding().map { it.value() },
        attachmentsHandler.attachmentState,
        ::IdentitySharedUiState
    )

    override fun updateLoadingState(loadingState: IsLoadingState) {
        isLoadingState.update { loadingState }
    }

    override suspend fun onItemSavedState(item: Item) {
        runCatching {
            renameAttachments(item.shareId, item.id)
        }.onFailure {
            PassLogger.w(TAG, "Error renaming attachments")
            PassLogger.w(TAG, it)
            snackbarDispatcher(ItemRenameAttachmentsError)
        }
        runCatching {
            linkAttachmentsToItem(item.shareId, item.id, item.revision)
        }.onFailure {
            PassLogger.w(TAG, "Link attachment error")
            PassLogger.w(TAG, it)
            snackbarDispatcher(ItemLinkAttachmentsError)
        }
        val itemSavedState = encryptionContextProvider.withEncryptionContext {
            ItemSavedState.Success(
                item.id,
                item.toUiModel(this@withEncryptionContext)
            )
        }
        isItemSavedState.update { itemSavedState }
    }

    override fun updateSelectedSection(customField: IdentityField.CustomField) {
        draftRepository.save(DRAFT_IDENTITY_CUSTOM_FIELD_KEY, customField)
    }

    override suspend fun onItemReceivedState(item: Item) {
        getItemAttachments(item)
        itemState.update { item.some() }
        val itemContents = encryptionContextProvider.withEncryptionContext {
            item.toItemContents<ItemContents.Identity> { decrypt(it) }
        }
        val personalDetails = itemContents.personalDetailsContent
        val addressDetails = itemContents.addressDetailsContent
        val contactDetails = itemContents.contactDetailsContent
        val workDetails = itemContents.workDetailsContent

        val fields = listOf(
            personalDetails.firstName to IdentityField.FirstName,
            personalDetails.middleName to IdentityField.MiddleName,
            personalDetails.lastName to IdentityField.LastName,
            personalDetails.birthdate to IdentityField.Birthdate,
            personalDetails.gender to IdentityField.Gender,
            addressDetails.floor to IdentityField.Floor,
            addressDetails.county to IdentityField.County,
            contactDetails.linkedin to IdentityField.Linkedin,
            contactDetails.reddit to IdentityField.Reddit,
            contactDetails.facebook to IdentityField.Facebook,
            contactDetails.yahoo to IdentityField.Yahoo,
            contactDetails.instagram to IdentityField.Instagram,
            workDetails.personalWebsite to IdentityField.PersonalWebsite,
            workDetails.workPhoneNumber to IdentityField.WorkPhoneNumber,
            workDetails.workEmail to IdentityField.WorkEmail
        )
        fields.forEach { (value, field) ->
            if (value.isNotBlank()) {
                identityFieldDraftRepository.addField(field, false)
            }
        }

        identityItemFormMutableState = IdentityItemFormState(
            title = itemContents.title,
            uiPersonalDetails = UIPersonalDetails(itemContents.personalDetailsContent).apply {
                copy(customFields = customFieldHandler.sanitiseForEditingCustomFields(this.customFields))
            },
            uiAddressDetails = UIAddressDetails(itemContents.addressDetailsContent).apply {
                copy(customFields = customFieldHandler.sanitiseForEditingCustomFields(this.customFields))
            },
            uiContactDetails = UIContactDetails(itemContents.contactDetailsContent).apply {
                copy(customFields = customFieldHandler.sanitiseForEditingCustomFields(this.customFields))
            },
            uiWorkDetails = UIWorkDetails(itemContents.workDetailsContent).apply {
                copy(customFields = customFieldHandler.sanitiseForEditingCustomFields(this.customFields))
            },
            uiExtraSections = itemContents.extraSectionContentList.map(::UIExtraSection)
                .map { it.copy(customFields = customFieldHandler.sanitiseForEditingCustomFields(it.customFields)) }
        )
    }

    private suspend fun getItemAttachments(item: Item) {
        runCatching {
            if (item.hasAttachments) {
                attachmentsHandler.getAttachmentsForItem(item.shareId, item.id)
            }
        }.onFailure {
            PassLogger.w(TAG, it)
            PassLogger.w(TAG, "Get attachments error")
            snackbarDispatcher(IdentitySnackbarMessage.AttachmentsInitError)
        }
    }

    override fun getReceivedItem(): Item =
        itemState.value.value() ?: throw IllegalStateException("Item is not received")

    override fun observeReceivedItem(): Flow<Option<Item>> = itemState

    override suspend fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        attachmentsHandler.openAttachment(contextHolder, attachment)
    }

    private fun updateCustomFieldState(field: IdentityField.CustomField, value: String): IdentityItemFormState {
        val identifier = CustomFieldIdentifier(
            index = field.index,
            type = field.customFieldType
        )
        val content = when (field.sectionType) {
            IdentitySectionType.PersonalDetails ->
                identityItemFormMutableState.uiPersonalDetails.customFields

            IdentitySectionType.ContactDetails ->
                identityItemFormMutableState.uiContactDetails.customFields

            IdentitySectionType.AddressDetails ->
                identityItemFormMutableState.uiAddressDetails.customFields

            IdentitySectionType.WorkDetails ->
                identityItemFormMutableState.uiWorkDetails.customFields

            is IdentitySectionType.ExtraSection ->
                identityItemFormMutableState.uiExtraSections[field.sectionType.index].customFields
        }

        val updated = customFieldHandler.onCustomFieldValueChanged(identifier, content, value)
        return when (field.sectionType) {
            IdentitySectionType.PersonalDetails -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = updated
                )
            )

            IdentitySectionType.ContactDetails -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = updated
                )
            )

            IdentitySectionType.AddressDetails -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = updated
                )
            )

            IdentitySectionType.WorkDetails -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = updated
                )
            )

            is IdentitySectionType.ExtraSection -> identityItemFormMutableState.copy(
                uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                    .apply {
                        set(
                            field.sectionType.index,
                            identityItemFormMutableState.uiExtraSections[field.sectionType.index]
                                .copy(customFields = updated)
                        )
                    }
            )
        }
    }

    private fun onUserEditedContent() {
        if (hasUserEditedContentState.value) return
        hasUserEditedContentState.update { true }
    }

    override fun observeActions(coroutineScope: CoroutineScope) {
        coroutineScope.launch { observeCustomFields() }
        observeNewAttachments(coroutineScope)
        observeHasDeletedAttachments(coroutineScope)
        observeHasRenamedAttachments(coroutineScope)
        coroutineScope.launch { observeNewExtraFields() }
    }

    private suspend fun observeNewExtraFields() {
        identityFieldDraftRepository.observeExtraFields()
            .runningFold(emptySet<IdentityField>()) { previous, current ->
                val added = current - previous
                if (added.isNotEmpty()) {
                    focusedFieldState.update { added.firstOrNull().toOption() }
                }
                current
            }
            .collect {}
    }

    private suspend fun observeCustomFields() {
        customFieldDraftRepository.observeAllEvents()
            .collectLatest {
                when (it) {
                    is DraftFormFieldEvent.FieldAdded -> onAddCustomField(it)
                    is DraftFormFieldEvent.FieldRemoved -> onRemoveCustomField(it)
                    is DraftFormFieldEvent.FieldRenamed -> onRenameCustomField(it)
                    is DraftFormSectionEvent.SectionAdded -> onAddExtraSection(it)
                    is DraftFormSectionEvent.SectionRemoved -> onRemoveCustomSection(it)
                    is DraftFormSectionEvent.SectionRenamed -> onRenameCustomSection(it)
                }
            }
    }

    private fun observeNewAttachments(coroutineScope: CoroutineScope) {
        attachmentsHandler.observeNewAttachments {
            onUserEditedContent()
            coroutineScope.launch {
                isLoadingState.update { IsLoadingState.Loading }
                attachmentsHandler.uploadNewAttachment(it.metadata)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
        }.launchIn(coroutineScope)
    }

    private fun observeHasDeletedAttachments(coroutineScope: CoroutineScope) {
        attachmentsHandler.observeHasDeletedAttachments {
            onUserEditedContent()
        }.launchIn(coroutineScope)
    }

    private fun observeHasRenamedAttachments(coroutineScope: CoroutineScope) {
        attachmentsHandler.observeHasRenamedAttachments {
            onUserEditedContent()
        }.launchIn(coroutineScope)
    }

    override suspend fun retryUploadDraftAttachment(metadata: FileMetadata) {
        isLoadingState.update { IsLoadingState.Loading }
        attachmentsHandler.uploadNewAttachment(metadata)
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    override suspend fun dismissFileAttachmentsOnboardingBanner() {
        userPreferencesRepository.setDisplayFileAttachmentsOnboarding(
            DisplayFileAttachmentsBanner.NotDisplay
        )
    }

    override fun onSocialSecurityNumberFieldFocusChange(isFocused: Boolean) {
        val socialSecurityNumber =
            identityItemFormMutableState.uiContactDetails.socialSecurityNumber

        identityItemFormMutableState = encryptionContextProvider.withEncryptionContext {
            decrypt(socialSecurityNumber.encrypted.toEncryptedByteArray())
                .let { decryptedSocialSecurityNumberByteArray ->
                    when {
                        decryptedSocialSecurityNumberByteArray.isEmpty() -> {
                            UIHiddenState.Empty(encrypted = socialSecurityNumber.encrypted)
                        }

                        isFocused -> {
                            UIHiddenState.Revealed(
                                encrypted = socialSecurityNumber.encrypted,
                                clearText = decryptedSocialSecurityNumberByteArray.decodeToString()
                            )
                        }

                        else -> {
                            UIHiddenState.Concealed(encrypted = socialSecurityNumber.encrypted)
                        }
                    }
                }
                .let { newSocialSecurityNumber ->
                    identityItemFormMutableState.copy(
                        uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                            socialSecurityNumber = newSocialSecurityNumber
                        )
                    )
                }
        }
    }

    override suspend fun pasteTotp() {
        withContext(appDispatchers.io) {
            onUserEditedContent()
            clipboardManager.getClipboardContent()
                .onSuccess { clipboardContent ->
                    withContext(appDispatchers.main) {
                        when (val field = focusedFieldState.value.value()) {
                            is IdentityField.CustomField -> {
                                val sanitisedContent = clipboardContent
                                    .replace(" ", "")
                                    .replace("\n", "")
                                onFieldChange(field, sanitisedContent)
                            }

                            else -> {}
                        }
                    }
                }
                .onFailure { PassLogger.d(TAG, it, "Failed on getting clipboard content") }
        }
    }

    private companion object {

        private const val TAG = "IdentityActionsProviderImpl"

    }

}

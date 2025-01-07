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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_TITLE_KEY
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_SECTION_TITLE_KEY
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_EXTRA_SECTION_KEY
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_SECTION_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CustomFieldIndexTitle
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemRenameAttachmentsError
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.AddressCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Birthdate
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ContactCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.County
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.CustomExtraField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ExtraSectionCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Facebook
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.FirstName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Floor
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Gender
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.IdentityFieldDraftRepository
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Instagram
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.LastName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Linkedin
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.MiddleName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalWebsite
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Reddit
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkCustomField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkEmail
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkPhoneNumber
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Yahoo
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.net.URI
import javax.inject.Inject

@Suppress("TooManyFunctions", "LargeClass")
@ViewModelScoped
class IdentityActionsProviderImpl @Inject constructor(
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val identityFieldDraftRepository: IdentityFieldDraftRepository,
    private val observeUpgradeInfo: ObserveUpgradeInfo,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val renameAttachments: RenameAttachments,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : IdentityActionsProvider {

    private var itemState: MutableStateFlow<Option<Item>> = MutableStateFlow(None)

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

    @Suppress("LongMethod")
    override fun onFieldChange(field: FieldChange) {
        onUserEditedContent()
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

    private fun onAddExtraSection(value: String) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections +
                listOf(UIExtraSection(value, emptyList()))
        )
    }

    private fun onRenameCustomSection(value: CustomFieldIndexTitle) {
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

    private fun onRemoveCustomSection(index: Int) {
        identityItemFormMutableState = identityItemFormMutableState.copy(
            uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                .apply { removeAt(index) }
        )
    }

    private fun onAddCustomField(value: CustomFieldContent, customExtraField: CustomExtraField) {
        val uiCustomFieldContent = UICustomFieldContent.from(value)
        identityItemFormMutableState = when (customExtraField) {
            is AddressCustomField -> {
                val addressDetails = identityItemFormMutableState.uiAddressDetails
                identityFieldDraftRepository.addCustomFieldIndex(addressDetails.customFields.size)
                identityItemFormMutableState.copy(
                    uiAddressDetails = addressDetails.copy(
                        customFields = addressDetails.customFields + listOf(uiCustomFieldContent)
                    )
                )
            }

            is ContactCustomField -> {
                val contactDetails = identityItemFormMutableState.uiContactDetails
                identityFieldDraftRepository.addCustomFieldIndex(contactDetails.customFields.size)
                identityItemFormMutableState.copy(
                    uiContactDetails = contactDetails.copy(
                        customFields = contactDetails.customFields + listOf(uiCustomFieldContent)
                    )
                )
            }

            is PersonalCustomField -> {
                val personalDetails = identityItemFormMutableState.uiPersonalDetails
                identityFieldDraftRepository.addCustomFieldIndex(personalDetails.customFields.size)
                identityItemFormMutableState.copy(
                    uiPersonalDetails = personalDetails.copy(
                        customFields = personalDetails.customFields + listOf(uiCustomFieldContent)
                    )
                )
            }

            is WorkCustomField -> {
                val workDetails = identityItemFormMutableState.uiWorkDetails
                identityFieldDraftRepository.addCustomFieldIndex(workDetails.customFields.size)
                identityItemFormMutableState.copy(
                    uiWorkDetails = workDetails.copy(
                        customFields = workDetails.customFields + listOf(uiCustomFieldContent)
                    )
                )
            }

            is ExtraSectionCustomField -> {
                val extraSection = identityItemFormMutableState.uiExtraSections
                identityFieldDraftRepository.addCustomFieldIndex(
                    extraSection[customExtraField.index].customFields.size
                )
                identityItemFormMutableState.copy(
                    uiExtraSections = extraSection.toMutableList()
                        .apply {
                            set(
                                customExtraField.index,
                                extraSection[customExtraField.index].copy(
                                    customFields = extraSection[customExtraField.index].customFields +
                                        listOf(uiCustomFieldContent)
                                )
                            )
                        }
                )
            }
        }
    }

    private fun onRemoveCustomField(index: Int, customExtraField: CustomExtraField) {
        identityItemFormMutableState = when (customExtraField) {
            is AddressCustomField -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is ContactCustomField -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is PersonalCustomField -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { removeAt(index) }
                )
            )

            is WorkCustomField -> identityItemFormMutableState.copy(
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
            is AddressCustomField ->
                identityItemFormMutableState.uiAddressDetails.customFields[value.index] to value.index

            is ContactCustomField ->
                identityItemFormMutableState.uiContactDetails.customFields[value.index] to value.index

            is PersonalCustomField ->
                identityItemFormMutableState.uiPersonalDetails.customFields[value.index] to value.index

            is WorkCustomField ->
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
            is AddressCustomField -> identityItemFormMutableState.copy(
                uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                    customFields = identityItemFormMutableState.uiAddressDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            is ContactCustomField -> identityItemFormMutableState.copy(
                uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                    customFields = identityItemFormMutableState.uiContactDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            is PersonalCustomField -> identityItemFormMutableState.copy(
                uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                    customFields = identityItemFormMutableState.uiPersonalDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            is WorkCustomField -> identityItemFormMutableState.copy(
                uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                    customFields = identityItemFormMutableState.uiWorkDetails.customFields.toMutableList()
                        .apply { set(index, updated) }
                )
            )

            is ExtraSectionCustomField -> {
                val sectionIndex = customExtraField.index

                val extraSections = identityItemFormMutableState.uiExtraSections
                val sectionToUpdate = extraSections[sectionIndex]
                val newSectionContent = sectionToUpdate.copy(
                    customFields = sectionToUpdate
                        .customFields
                        .toMutableList()
                        .apply { set(index, updated) }
                )
                val updatedExtraSections = extraSections.toMutableList()
                    .apply { set(sectionIndex, newSectionContent) }

                identityItemFormMutableState.copy(uiExtraSections = updatedExtraSections)
            }
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
        attachmentsHandler.onClearAttachments()
    }

    @Suppress("LongMethod")
    override fun onCustomFieldFocusChange(
        index: Int,
        focused: Boolean,
        customExtraField: CustomExtraField
    ) {
        identityItemFormMutableState = when (customExtraField) {
            AddressCustomField -> handleFieldFocusChange(
                index,
                focused,
                identityItemFormMutableState.uiAddressDetails.customFields
            ) { updatedFields ->
                identityItemFormMutableState.copy(
                    uiAddressDetails = identityItemFormMutableState.uiAddressDetails.copy(
                        customFields = updatedFields
                    )
                )
            }

            ContactCustomField -> handleFieldFocusChange(
                index,
                focused,
                identityItemFormMutableState.uiContactDetails.customFields
            ) { updatedFields ->
                identityItemFormMutableState.copy(
                    uiContactDetails = identityItemFormMutableState.uiContactDetails.copy(
                        customFields = updatedFields
                    )
                )
            }

            PersonalCustomField -> handleFieldFocusChange(
                index,
                focused,
                identityItemFormMutableState.uiPersonalDetails.customFields
            ) { updatedFields ->
                identityItemFormMutableState.copy(
                    uiPersonalDetails = identityItemFormMutableState.uiPersonalDetails.copy(
                        customFields = updatedFields
                    )
                )
            }

            WorkCustomField -> handleFieldFocusChange(
                index,
                focused,
                identityItemFormMutableState.uiWorkDetails.customFields
            ) { updatedFields ->
                identityItemFormMutableState.copy(
                    uiWorkDetails = identityItemFormMutableState.uiWorkDetails.copy(
                        customFields = updatedFields
                    )
                )
            }

            is ExtraSectionCustomField -> {
                if (customExtraField.index >= identityItemFormMutableState.uiExtraSections.size) {
                    identityItemFormMutableState
                } else {
                    handleFieldFocusChange(
                        index,
                        focused,
                        identityItemFormMutableState.uiExtraSections[customExtraField.index].customFields
                    ) { updatedFields ->
                        identityItemFormMutableState.copy(
                            uiExtraSections = identityItemFormMutableState.uiExtraSections.toMutableList()
                                .apply {
                                    set(
                                        customExtraField.index,
                                        identityItemFormMutableState.uiExtraSections[customExtraField.index].copy(
                                            customFields = updatedFields
                                        )
                                    )
                                }
                        )
                    }
                }
            }
        }
    }

    override fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(contextHolder, uri, mimetype)
    }

    private fun handleFieldFocusChange(
        index: Int,
        focused: Boolean,
        customFields: List<UICustomFieldContent>,
        updateState: (List<UICustomFieldContent>) -> IdentityItemFormState
    ): IdentityItemFormState {
        if (index >= customFields.size) return identityItemFormMutableState

        val fieldContent: UICustomFieldContent = customFields[index]
        return if (fieldContent is UICustomFieldContent.Hidden) {
            val fieldChange = when {
                fieldContent.value is UIHiddenState.Empty -> fieldContent
                focused -> encryptionContextProvider.withEncryptionContext {
                    fieldContent.copy(
                        value = UIHiddenState.Revealed(
                            encrypted = fieldContent.value.encrypted,
                            clearText = decrypt(fieldContent.value.encrypted)
                        )
                    )
                }

                else -> fieldContent.copy(
                    value = UIHiddenState.Concealed(
                        encrypted = fieldContent.value.encrypted
                    )
                )
            }
            updateState(customFields.toMutableList().apply { set(index, fieldChange) })
        } else {
            identityItemFormMutableState
        }
    }

    override fun observeSharedState(): Flow<IdentitySharedUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState.map { it.toPersistentSet() },
        isItemSavedState,
        identityFieldDraftRepository.observeExtraFields().map(Set<ExtraField>::toPersistentSet),
        identityFieldDraftRepository.observeLastAddedExtraField(),
        observeUpgradeInfo().distinctUntilChanged().asLoadingResult().map(::canUseCustomFields),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding().map { it.value() },
        featureFlagsRepository[FeatureFlag.FILE_ATTACHMENTS_V1],
        attachmentsHandler.attachmentState,
        ::IdentitySharedUiState
    )

    private fun canUseCustomFields(result: LoadingResult<UpgradeInfo>) =
        result.getOrNull()?.plan?.let { it.isPaidPlan || it.isTrialPlan } ?: false

    override fun updateLoadingState(loadingState: IsLoadingState) {
        isLoadingState.update { loadingState }
    }

    override suspend fun onItemSavedState(item: Item) {
        if (isFileAttachmentsEnabled()) {
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
        }
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

    override suspend fun onItemReceivedState(item: Item) {
        getItemAttachments(item)
        itemState.update { item.some() }
        val itemContents = encryptionContextProvider.withEncryptionContext {
            toItemContents(
                itemType = item.itemType,
                encryptionContext = this,
                title = item.title,
                note = item.note,
                flags = item.flags
            ) as ItemContents.Identity
        }
        val personalDetails = itemContents.personalDetailsContent
        val addressDetails = itemContents.addressDetailsContent
        val contactDetails = itemContents.contactDetailsContent
        val workDetails = itemContents.workDetailsContent

        val fields = listOf(
            personalDetails.firstName to FirstName,
            personalDetails.middleName to MiddleName,
            personalDetails.lastName to LastName,
            personalDetails.birthdate to Birthdate,
            personalDetails.gender to Gender,
            addressDetails.floor to Floor,
            addressDetails.county to County,
            contactDetails.linkedin to Linkedin,
            contactDetails.reddit to Reddit,
            contactDetails.facebook to Facebook,
            contactDetails.yahoo to Yahoo,
            contactDetails.instagram to Instagram,
            workDetails.personalWebsite to PersonalWebsite,
            workDetails.workPhoneNumber to WorkPhoneNumber,
            workDetails.workEmail to WorkEmail
        )
        fields.forEach { (value, field) ->
            if (value.isNotBlank()) {
                identityFieldDraftRepository.addField(field, false)
            }
        }

        if (personalDetails.customFields.isNotEmpty()) {
            identityFieldDraftRepository.addField(PersonalCustomField, false)
        }
        if (addressDetails.customFields.isNotEmpty()) {
            identityFieldDraftRepository.addField(AddressCustomField, false)
        }
        if (contactDetails.customFields.isNotEmpty()) {
            identityFieldDraftRepository.addField(ContactCustomField, false)
        }
        if (workDetails.customFields.isNotEmpty()) {
            identityFieldDraftRepository.addField(WorkCustomField, false)
        }
        if (itemContents.extraSectionContentList.isNotEmpty()) {
            itemContents.extraSectionContentList.forEachIndexed { index, _ ->
                identityFieldDraftRepository.addField(ExtraSectionCustomField(index), false)
            }
        }
        identityItemFormMutableState = IdentityItemFormState(itemContents)
    }

    private suspend fun getItemAttachments(item: Item) {
        runCatching {
            val isFileAttachmentsEnabled =
                featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1)
                    .firstOrNull()
                    ?: false
            if (item.hasAttachments && isFileAttachmentsEnabled) {
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

    override fun resetLastAddedFieldFocus() {
        identityFieldDraftRepository.resetLastAddedExtraField()
    }

    override suspend fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        attachmentsHandler.openAttachment(contextHolder, attachment)
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
                        value = if (field.value.isBlank()) {
                            UIHiddenState.Empty(encrypt(""))
                        } else {
                            UIHiddenState.Revealed(
                                encrypted = encrypt(field.value),
                                clearText = field.value
                            )
                        }
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

    private suspend fun observeNewCustomField() {
        draftRepository.get<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY)
                val extraFieldType =
                    draftRepository.delete<CustomExtraField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
                if (extraFieldType !is Some) return@collect
                onAddCustomField(it.value, extraFieldType.value)
            }
    }

    private suspend fun observeNewExtraSection() {
        draftRepository.get<String>(DRAFT_IDENTITY_EXTRA_SECTION_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<String>(DRAFT_IDENTITY_EXTRA_SECTION_KEY)
                onAddExtraSection(it.value)
            }
    }

    private suspend fun observeRemoveCustomField() {
        draftRepository.get<Int>(DRAFT_REMOVE_CUSTOM_FIELD_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<Int>(DRAFT_REMOVE_CUSTOM_FIELD_KEY)
                val extraFieldType =
                    draftRepository.delete<CustomExtraField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
                if (extraFieldType !is Some) return@collect
                onRemoveCustomField(it.value, extraFieldType.value)
            }
    }

    private suspend fun observeRemoveExtraSection() {
        draftRepository.get<Int>(DRAFT_REMOVE_CUSTOM_SECTION_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<Int>(DRAFT_REMOVE_CUSTOM_SECTION_KEY)
                onRemoveCustomSection(it.value)
            }
    }

    private suspend fun observeRenameCustomField() {
        draftRepository.get<CustomFieldIndexTitle>(DRAFT_CUSTOM_FIELD_TITLE_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<CustomFieldIndexTitle>(DRAFT_CUSTOM_FIELD_TITLE_KEY)
                val extraFieldType =
                    draftRepository.delete<CustomExtraField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
                if (extraFieldType !is Some) return@collect
                onRenameCustomField(it.value, extraFieldType.value)
            }
    }

    private suspend fun observeRenameExtraSection() {
        draftRepository.get<CustomFieldIndexTitle>(DRAFT_CUSTOM_SECTION_TITLE_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<CustomFieldIndexTitle>(DRAFT_CUSTOM_SECTION_TITLE_KEY)
                onRenameCustomSection(it.value)
            }
    }

    private fun onUserEditedContent() {
        if (hasUserEditedContentState.value) return
        hasUserEditedContentState.update { true }
    }

    override fun observeActions(coroutineScope: CoroutineScope) {
        coroutineScope.launch { observeNewCustomField() }
        coroutineScope.launch { observeRemoveCustomField() }
        coroutineScope.launch { observeRenameCustomField() }
        coroutineScope.launch { observeNewExtraSection() }
        coroutineScope.launch { observeRemoveExtraSection() }
        coroutineScope.launch { observeRenameExtraSection() }
        observeNewAttachments(coroutineScope)
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

    suspend fun isFileAttachmentsEnabled(): Boolean =
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1).firstOrNull() ?: false

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

    companion object {
        private const val TAG = "IdentityActionsProviderImpl"
    }
}

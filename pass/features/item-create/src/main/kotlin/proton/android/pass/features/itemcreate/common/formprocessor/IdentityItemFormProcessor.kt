/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.common.formprocessor

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentityItemFormProcessor @Inject constructor(
    private val customFieldFormProcessor: CustomFieldFormProcessorType,
    private val sectionFormProcessor: SectionFormProcessorType
) : FormProcessor<IdentityItemFormProcessor.Input, IdentityItemFormState> {

    data class Input(
        val originalPersonalCustomFields: List<UICustomFieldContent>,
        val originalAddressCustomFields: List<UICustomFieldContent>,
        val originalContactCustomFields: List<UICustomFieldContent>,
        val originalWorkCustomFields: List<UICustomFieldContent>,
        val originalSections: List<UIExtraSection>,
        val formState: IdentityItemFormState
    )

    @Suppress("LongMethod", "ComplexCondition")
    override suspend fun process(
        input: Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<IdentityItemFormState> {
        val errors = mutableSetOf<ValidationError>()

        if (input.formState.title.isBlank()) {
            errors += CommonFieldValidationError.BlankTitle
        }
        val personalCustomFieldsResult = customFieldFormProcessor.process(
            input = UICustomFieldContentFormProcessor.Input(
                customFields = input.formState.uiPersonalDetails.customFields,
                originalCustomFields = input.originalPersonalCustomFields
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (personalCustomFieldsResult is FormProcessingResult.Error) {
            errors.addAll(personalCustomFieldsResult.errors)
        }
        val addressCustomFieldsResult = customFieldFormProcessor.process(
            input = UICustomFieldContentFormProcessor.Input(
                customFields = input.formState.uiAddressDetails.customFields,
                originalCustomFields = input.originalAddressCustomFields
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (addressCustomFieldsResult is FormProcessingResult.Error) {
            errors.addAll(addressCustomFieldsResult.errors)
        }
        val contactCustomFieldsResult = customFieldFormProcessor.process(
            input = UICustomFieldContentFormProcessor.Input(
                customFields = input.formState.uiContactDetails.customFields,
                originalCustomFields = input.originalContactCustomFields
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (contactCustomFieldsResult is FormProcessingResult.Error) {
            errors.addAll(contactCustomFieldsResult.errors)
        }
        val workCustomFieldsResult = customFieldFormProcessor.process(
            input = UICustomFieldContentFormProcessor.Input(
                customFields = input.formState.uiWorkDetails.customFields,
                originalCustomFields = input.originalWorkCustomFields
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (workCustomFieldsResult is FormProcessingResult.Error) {
            errors.addAll(workCustomFieldsResult.errors)
        }
        val sectionResult = sectionFormProcessor.process(
            input = UISectionContentFormProcessor.Input(
                sections = input.formState.uiExtraSections,
                originalSections = input.originalSections
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (sectionResult is FormProcessingResult.Error) {
            errors.addAll(sectionResult.errors)
        }
        return if (
            errors.isEmpty() &&
            personalCustomFieldsResult is FormProcessingResult.Success &&
            addressCustomFieldsResult is FormProcessingResult.Success &&
            contactCustomFieldsResult is FormProcessingResult.Success &&
            workCustomFieldsResult is FormProcessingResult.Success &&
            sectionResult is FormProcessingResult.Success
        ) {
            FormProcessingResult.Success(
                input.formState.copy(
                    uiPersonalDetails = input.formState.uiPersonalDetails.copy(
                        customFields = personalCustomFieldsResult.sanitized
                    ),
                    uiAddressDetails = input.formState.uiAddressDetails.copy(
                        customFields = addressCustomFieldsResult.sanitized
                    ),
                    uiContactDetails = input.formState.uiContactDetails.copy(
                        customFields = contactCustomFieldsResult.sanitized
                    ),
                    uiWorkDetails = input.formState.uiWorkDetails.copy(
                        customFields = workCustomFieldsResult.sanitized
                    ),
                    uiExtraSections = sectionResult.sanitized
                )
            )
        } else {
            FormProcessingResult.Error(errors)
        }
    }

}

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
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomItemFormProcessor @Inject constructor(
    private val customFieldFormProcessor: CustomFieldFormProcessorType,
    private val sectionFormProcessor: SectionFormProcessorType
) : FormProcessor<CustomItemFormProcessor.Input, ItemFormState> {

    data class Input(
        val originalCustomFields: List<UICustomFieldContent>,
        val originalSections: List<UIExtraSection>,
        val formState: ItemFormState
    )

    override suspend fun process(
        input: Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<ItemFormState> {
        val errors = mutableSetOf<ValidationError>()

        if (input.formState.title.isBlank()) {
            errors += CommonFieldValidationError.BlankTitle
        }
        val customFieldResult = customFieldFormProcessor.process(
            input = UICustomFieldContentFormProcessor.Input(
                customFields = input.formState.customFieldList,
                originalCustomFields = input.originalCustomFields
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (customFieldResult is FormProcessingResult.Error) {
            errors.addAll(customFieldResult.errors)
        }
        val sectionResult = sectionFormProcessor.process(
            input = UISectionContentFormProcessor.Input(
                sections = input.formState.sectionList,
                originalSections = input.originalSections
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (customFieldResult is FormProcessingResult.Error) {
            errors.addAll(customFieldResult.errors)
        }
        return if (
            errors.isEmpty() &&
            customFieldResult is FormProcessingResult.Success &&
            sectionResult is FormProcessingResult.Success
        ) {
            FormProcessingResult.Success(
                input.formState.copy(
                    customFieldList = customFieldResult.sanitized,
                    sectionList = sectionResult.sanitized
                )
            )
        } else {
            FormProcessingResult.Error(errors)
        }
    }

}

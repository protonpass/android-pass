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
import proton.android.pass.common.api.some
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.ValidationError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UISectionContentFormProcessor @Inject constructor(
    private val custommFieldProcessor: CustomFieldFormProcessorType
) : FormProcessor<UISectionContentFormProcessor.Input, List<UIExtraSection>> {

    data class Input(
        val sections: List<UIExtraSection>,
        val originalSections: List<UIExtraSection>
    )

    override suspend fun process(
        input: Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<List<UIExtraSection>> {
        val allErrors = mutableSetOf<ValidationError>()

        val sanitised = input.sections.mapIndexed { sectionIndex, section ->
            val originalFields = input.originalSections.getOrNull(sectionIndex)
                ?.customFields
                .orEmpty()
                .filterIsInstance<UICustomFieldContent.Totp>()
                .associateBy { it.id }

            val result = custommFieldProcessor.process(
                input = UICustomFieldContentFormProcessor.Input(
                    sectionIndex = sectionIndex.some(),
                    customFields = section.customFields,
                    originalCustomFields = originalFields.values.toList()
                ),
                decrypt = decrypt,
                encrypt = encrypt
            )
            when (result) {
                is FormProcessingResult.Error -> {
                    allErrors.addAll(result.errors)
                    section
                }

                is FormProcessingResult.Success ->
                    section.copy(customFields = result.sanitized)
            }
        }

        return if (allErrors.isEmpty()) {
            FormProcessingResult.Success(sanitised)
        } else {
            FormProcessingResult.Error(allErrors)
        }
    }
}

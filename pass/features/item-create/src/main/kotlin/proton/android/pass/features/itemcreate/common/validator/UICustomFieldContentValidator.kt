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

package proton.android.pass.features.itemcreate.common.validator

import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UICustomFieldContentValidator @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val totpManager: TotpManager
) : Validator<UICustomFieldContentValidator.Input> {

    data class Input(
        val customFields: List<UICustomFieldContent>,
        val originalCustomFields: List<UICustomFieldContent>,
        val sections: List<UIExtraSection>,
        val originalSections: List<UIExtraSection>
    )

    override suspend fun validate(input: Input): Set<ValidationError> =
        encryptionContextProvider.withEncryptionContextSuspendable {
            val allErrors = mutableSetOf<ValidationError>()

            allErrors += validateTotpFields(
                entries = input.customFields,
                originalEntriesById = input.originalCustomFields
                    .filterIsInstance<UICustomFieldContent.Totp>()
                    .associateBy { it.id },
                sectionIndex = None,
                encryptionContext = this
            )

            input.sections.forEachIndexed { sectionIndex, section ->
                val originalFields = input.originalSections.getOrNull(sectionIndex)
                    ?.customFields
                    .orEmpty()
                    .filterIsInstance<UICustomFieldContent.Totp>()
                    .associateBy { it.id }

                allErrors += validateTotpFields(
                    entries = section.customFields,
                    originalEntriesById = originalFields,
                    sectionIndex = sectionIndex.some(),
                    encryptionContext = this
                )
            }

            allErrors.toSet()
        }

    private suspend fun validateTotpFields(
        entries: List<UICustomFieldContent>,
        originalEntriesById: Map<String, UICustomFieldContent.Totp>,
        sectionIndex: Option<Int>,
        encryptionContext: EncryptionContext
    ): Set<ValidationError> {
        val errors = mutableSetOf<ValidationError>()

        entries.forEachIndexed { index, entry ->
            if (entry !is UICustomFieldContent.Totp) return@forEachIndexed

            val decrypted = encryptionContext.decrypt(entry.value.encrypted)
            if (decrypted.isBlank()) {
                errors.add(CustomFieldValidationError.EmptyField(sectionIndex, index))
                return@forEachIndexed
            }

            val originalDecrypted = originalEntriesById[entry.id]
                ?.let { encryptionContext.decrypt(it.value.encrypted) }
                .orEmpty()

            val result = totpManager.sanitiseToSave(originalDecrypted, decrypted)

            result.fold(
                onSuccess = { uri ->
                    val parseSuccess = totpManager.parse(uri).isSuccess
                    val codeSuccess = runCatching {
                        totpManager.observeCode(uri).firstOrNull()
                    }.isSuccess

                    if (!parseSuccess || !codeSuccess) {
                        errors.add(CustomFieldValidationError.InvalidTotp(sectionIndex, index))
                    }
                },
                onFailure = {
                    errors.add(CustomFieldValidationError.InvalidTotp(sectionIndex, index))
                }
            )
        }

        return errors
    }
}

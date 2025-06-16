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

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError.InvalidTotp
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UICustomFieldContentFormProcessor @Inject constructor(
    private val totpManager: TotpManager
) : FormProcessor<UICustomFieldContentFormProcessor.Input, List<UICustomFieldContent>> {

    data class Input(
        val sectionIndex: Option<Int> = None,
        val customFields: List<UICustomFieldContent>,
        val originalCustomFields: List<UICustomFieldContent>
    )

    override suspend fun process(
        input: Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<List<UICustomFieldContent>> {
        val allErrors = mutableSetOf<ValidationError>()

        val originalEntriesById = input.originalCustomFields
            .filterIsInstance<UICustomFieldContent.Totp>()
            .associateBy { it.id }
        val sanitisedCustomFields = input.customFields.mapIndexed { index, entry ->
            when (entry) {
                is UICustomFieldContent.Totp -> {
                    val decrypted = decrypt(entry.value.encrypted)
                    val originalDecrypted = originalEntriesById[entry.id]
                        ?.let { decrypt(it.value.encrypted) }
                        .orEmpty()

                    totpManager.sanitiseToSave(originalDecrypted, decrypted).fold(
                        onSuccess = { uri ->
                            val parseSuccess = totpManager.parse(uri).isSuccess
                            val codeSuccess = runCatching {
                                totpManager.observeCode(uri).firstOrNull()
                            }.isSuccess

                            if (!parseSuccess || !codeSuccess) {
                                allErrors.add(InvalidTotp(input.sectionIndex, index))
                            }

                            entry.copy(value = UIHiddenState.Revealed(encrypt(uri), uri))
                        },
                        onFailure = {
                            allErrors.add(InvalidTotp(input.sectionIndex, index))
                            entry
                        }
                    )
                }
                else -> entry
            }
        }

        return if (allErrors.isEmpty()) {
            FormProcessingResult.Success(sanitisedCustomFields)
        } else {
            FormProcessingResult.Error(allErrors)
        }
    }
}

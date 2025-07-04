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

package proton.android.pass.features.itemcreate.common.customfields

import dagger.hilt.android.scopes.ViewModelScoped
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent.Companion.createCustomField
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

interface CustomFieldHandler {

    fun onCustomFieldAdded(label: String, customFieldType: CustomFieldType): UICustomFieldContent

    fun onCustomFieldRenamed(
        customFieldList: List<UICustomFieldContent>,
        index: Int,
        newLabel: String
    ): List<UICustomFieldContent>

    fun onCustomFieldValueChanged(
        customFieldIdentifier: CustomFieldIdentifier,
        customFieldList: List<UICustomFieldContent>,
        value: String
    ): List<UICustomFieldContent>

    fun onCustomFieldFocusedChanged(
        customFieldIdentifier: CustomFieldIdentifier,
        customFieldList: List<UICustomFieldContent>,
        isFocused: Boolean
    ): List<UICustomFieldContent>

    fun sanitiseForEditingCustomFields(customFieldList: List<UICustomFieldContent>): List<UICustomFieldContent>
}

@ViewModelScoped
class CustomFieldHandlerImpl @Inject constructor(
    private val totpManager: TotpManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : CustomFieldHandler {

    override fun onCustomFieldAdded(label: String, customFieldType: CustomFieldType): UICustomFieldContent =
        encryptionContextProvider.withEncryptionContext {
            createCustomField(customFieldType, label, this)
        }

    override fun onCustomFieldRenamed(
        customFieldList: List<UICustomFieldContent>,
        index: Int,
        newLabel: String
    ): List<UICustomFieldContent> = customFieldList.toMutableList().apply {
        set(index, this[index].updateLabel(newLabel))
    }

    override fun onCustomFieldValueChanged(
        customFieldIdentifier: CustomFieldIdentifier,
        customFieldList: List<UICustomFieldContent>,
        value: String
    ): List<UICustomFieldContent> {
        if (customFieldIdentifier.index !in customFieldList.indices) return customFieldList
        val updated = when (val field = customFieldList[customFieldIdentifier.index]) {
            is UICustomFieldContent.Hidden -> {
                UICustomFieldContent.Hidden(
                    label = field.label,
                    value = createHiddenState(value)
                )
            }

            is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                label = field.label,
                value = value
            )

            is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                label = field.label,
                value = createHiddenState(value),
                id = field.id
            )

            is UICustomFieldContent.Date -> UICustomFieldContent.Date(
                label = field.label,
                value = value.toLongOrNull()
            )
        }
        return customFieldList.toMutableList().apply {
            this[customFieldIdentifier.index] = updated
        }
    }

    override fun onCustomFieldFocusedChanged(
        customFieldIdentifier: CustomFieldIdentifier,
        customFieldList: List<UICustomFieldContent>,
        isFocused: Boolean
    ): List<UICustomFieldContent> {
        if (customFieldIdentifier.type == CustomFieldType.Totp) return customFieldList
        return customFieldList.mapIndexed fields@{ customFieldIndex, field ->
            when {
                customFieldIndex != customFieldIdentifier.index || field !is UICustomFieldContent.Hidden -> field
                field.value is UIHiddenState.Empty -> field
                isFocused -> encryptionContextProvider.withEncryptionContext {
                    field.copy(
                        value = UIHiddenState.Revealed(
                            encrypted = field.value.encrypted,
                            clearText = decrypt(field.value.encrypted)
                        )
                    )
                }

                else -> field.copy(
                    value = UIHiddenState.Concealed(encrypted = field.value.encrypted)
                )
            }
        }
    }

    override fun sanitiseForEditingCustomFields(
        customFieldList: List<UICustomFieldContent>
    ): List<UICustomFieldContent> = encryptionContextProvider.withEncryptionContext {
        customFieldList.map { sanitizeField(it, ::decrypt, ::encrypt) }
    }

    private fun sanitizeField(
        field: UICustomFieldContent,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): UICustomFieldContent = when (field) {
        is UICustomFieldContent.Totp -> sanitizeTotp(field, decrypt, encrypt)
        else -> field
    }

    private fun sanitizeTotp(
        field: UICustomFieldContent.Totp,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): UICustomFieldContent.Totp {
        val rawUri = field.value.extract(decrypt = decrypt)
        return if (rawUri.isBlank()) {
            field
        } else {
            val cleaned = totpManager.sanitiseToEdit(rawUri).getOrNull() ?: rawUri
            field.copy(
                value = UIHiddenState.Revealed(
                    encrypted = encrypt(cleaned),
                    clearText = cleaned
                )
            )
        }
    }

    private fun UIHiddenState.extract(decrypt: (String) -> String): String = when (this) {
        is UIHiddenState.Concealed -> decrypt(encrypted)
        is UIHiddenState.Revealed -> clearText
        is UIHiddenState.Empty -> ""
    }

    private fun createHiddenState(value: String): UIHiddenState = encryptionContextProvider.withEncryptionContext {
        when {
            value.isBlank() -> UIHiddenState.Empty(encrypt(""))
            else -> UIHiddenState.Revealed(
                encrypted = encrypt(value),
                clearText = value
            )
        }
    }
}

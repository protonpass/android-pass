/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill.ui.autofill

import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.log.api.PassLogger

object ItemFieldMapper {

    private const val TAG = "ItemFieldMapper"

    private data class AutofillFieldMapping(
        val autofillFieldId: AndroidAutofillFieldId?,
        val autofillType: FieldType,
        val isFocused: Boolean,
        val parentId: AndroidAutofillFieldId?
    )

    @Suppress("LongParameterList", "LongMethod", "ComplexMethod")
    fun mapFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem,
        androidAutofillFieldIds: List<AndroidAutofillFieldId?>,
        autofillTypes: List<FieldType>,
        fieldIsFocusedList: List<Boolean>,
        parentIdList: List<AndroidAutofillFieldId?>
    ): AutofillMappings {
        val mappingList = mutableListOf<DatasetMapping>()
        val fields = androidAutofillFieldIds
            .zip(autofillTypes)
            .zip(fieldIsFocusedList)
            .zip(parentIdList) { (pair, isFocused), parentId ->
                AutofillFieldMapping(pair.first, pair.second, isFocused, parentId)
            }
        val usernameFields =
            fields.filter { it.autofillType == FieldType.Username || it.autofillType == FieldType.Email }
        val passwordFields = fields.filter { it.autofillType == FieldType.Password }
        val isFocusedField = fields.firstOrNull { it.isFocused }

        when {
            isFocusedField?.autofillFieldId != null && usernameFields.contains(isFocusedField) ->
                mappingList.add(
                    DatasetMapping(
                        autofillFieldId = isFocusedField.autofillFieldId,
                        contents = autofillItem.username,
                        displayValue = autofillItem.username
                    )
                )

            isFocusedField?.autofillFieldId != null &&
                passwordFields.contains(isFocusedField) &&
                autofillItem.password != null ->
                mappingList.add(
                    DatasetMapping(
                        autofillFieldId = isFocusedField.autofillFieldId,
                        contents = encryptionContext.decrypt(autofillItem.password),
                        displayValue = ""
                    )
                )
        }

        if (usernameFields.isNotEmpty() && !usernameFields.contains(isFocusedField)) {
            val usernameField = usernameFields
                .firstOrNull { it.parentId == isFocusedField?.parentId }
                ?: usernameFields.first()
            usernameField.autofillFieldId?.let {
                mappingList.add(
                    DatasetMapping(
                        autofillFieldId = usernameField.autofillFieldId,
                        contents = autofillItem.username,
                        displayValue = autofillItem.username
                    )
                )
            }
        }
        if (passwordFields.isNotEmpty() && !passwordFields.contains(isFocusedField) && autofillItem.password != null) {
            val passwordField = passwordFields
                .firstOrNull { it.parentId == isFocusedField?.parentId }
                ?: passwordFields.first()
            passwordField.autofillFieldId?.let {
                mappingList.add(
                    DatasetMapping(
                        autofillFieldId = passwordField.autofillFieldId,
                        contents = encryptionContext.decrypt(autofillItem.password),
                        displayValue = ""
                    )
                )
            }
        }

        if (mappingList.isEmpty()) {
            val message = "No mappings found for autofill. Detected field types: $autofillTypes"
            PassLogger.e(TAG, IllegalStateException(message), message)
        }

        return AutofillMappings(mappingList)
    }
}

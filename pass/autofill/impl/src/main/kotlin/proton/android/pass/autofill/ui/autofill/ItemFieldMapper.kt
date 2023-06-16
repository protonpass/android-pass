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

    fun mapFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem,
        androidAutofillFieldIds: List<AndroidAutofillFieldId>,
        autofillTypes: List<FieldType>
    ): AutofillMappings {
        val mappingList = mutableListOf<DatasetMapping>()
        var loginIndex = autofillTypes.indexOfFirst { it == FieldType.Email }
        if (loginIndex == -1) {
            loginIndex = autofillTypes.indexOfFirst { it == FieldType.Username }
        }
        if (loginIndex != -1) {
            mappingList.add(
                DatasetMapping(
                    autofillFieldId = androidAutofillFieldIds[loginIndex],
                    contents = autofillItem.username,
                    displayValue = autofillItem.username
                )
            )
        }
        val passwordIndex = autofillTypes.indexOfFirst { it == FieldType.Password }
        if (passwordIndex != -1 && autofillItem.password != null) {
            mappingList.add(
                DatasetMapping(
                    autofillFieldId = androidAutofillFieldIds[passwordIndex],
                    contents = encryptionContext.decrypt(autofillItem.password),
                    displayValue = ""
                )
            )
        }

        if (mappingList.isEmpty()) {
            val message = "No mappings found for autofill. Detected field types: $autofillTypes"
            PassLogger.e(TAG, IllegalStateException(message), message)
        }

        return AutofillMappings(mappingList)
    }
}

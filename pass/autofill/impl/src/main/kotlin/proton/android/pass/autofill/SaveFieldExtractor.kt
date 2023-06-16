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

package proton.android.pass.autofill

import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.entities.SaveItemType

object SaveFieldExtractor {
    fun extract(fieldsToSave: List<AssistField>): List<SaveInformation> {
        val isIdentity: (AssistField) -> Boolean = {
            listOf(FieldType.Username, FieldType.Email).contains(it.type)
        }

        val validFields = fieldsToSave.filter { it.type != null }

        val passwordField = validFields.firstOrNull { it.type == FieldType.Password }
        var identityField = validFields.firstOrNull(isIdentity)

        // If we have 2 fields, it's highly likely that the other one will be a username / email
        if (identityField == null && passwordField != null && validFields.count() == 2) {
            identityField = validFields.firstOrNull {
                it !== passwordField && it.type != FieldType.Password
            }
        }

        val identityValue = identityField?.text
        val passwordValue = passwordField?.text

        return if (identityValue != null && passwordValue != null) {
            listOf(
                SaveInformation(SaveItemType.Login(identityValue, passwordValue))
            )
        } else {
            validFields.map {
                SaveInformation(SaveItemType.SingleValue(it.text.toString()))
            }
        }
    }
}

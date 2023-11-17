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

package proton.android.pass.autofill.heuristics

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.heuristics.HeuristicsUtils.findNearestNodeByParentId
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.log.api.PassLogger

object ItemFieldMapper {

    private const val TAG = "ItemFieldMapper"

    private data class AutofillFieldMapping(
        val autofillFieldId: AutofillFieldId?,
        val autofillType: FieldType,
        val isFocused: Boolean,
        val nodePath: List<AutofillFieldId?>
    ) : IdentifiableNode {
        override val nodeId = autofillFieldId
        override val parentPath = nodePath
    }

    @Suppress("LongParameterList")
    fun mapFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem,
        androidAutofillFieldIds: List<AutofillFieldId?>,
        autofillTypes: List<FieldType>,
        fieldIsFocusedList: List<Boolean>,
        parentIdList: List<List<AutofillFieldId>>
    ): AutofillMappings = when (autofillItem) {
        is AutofillItem.Login -> {
            mapLoginFields(
                encryptionContext = encryptionContext,
                autofillItem = autofillItem,
                androidAutofillFieldIds = androidAutofillFieldIds,
                autofillTypes = autofillTypes,
                fieldIsFocusedList = fieldIsFocusedList,
                parentIdList = parentIdList,
            )
        }
    }

    @Suppress("LongParameterList")
    private fun mapLoginFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        androidAutofillFieldIds: List<AutofillFieldId?>,
        autofillTypes: List<FieldType>,
        fieldIsFocusedList: List<Boolean>,
        parentIdList: List<List<AutofillFieldId>>
    ): AutofillMappings {
        val fields = mapToFields(
            androidAutofillFieldIds = androidAutofillFieldIds,
            autofillTypes = autofillTypes,
            fieldIsFocusedList = fieldIsFocusedList,
            parentIdList = parentIdList
        )
        val usernameFields = fields.filter {
            it.autofillType == FieldType.Username || it.autofillType == FieldType.Email
        }
        val passwordFields = fields.filter { it.autofillType == FieldType.Password }
        val focusedField = fields.firstOrNull { it.isFocused }

        val mappingList = performMappings(
            encryptionContext = encryptionContext,
            autofillItem = autofillItem,
            usernameFields = usernameFields,
            passwordFields = passwordFields,
            focusedField = focusedField,
        )

        if (mappingList.isEmpty()) {
            val message = "No mappings found for autofill. Detected field types: $autofillTypes"
            PassLogger.e(TAG, IllegalStateException(message), message)
        }

        return AutofillMappings(mappingList)
    }

    private fun performMappings(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        usernameFields: List<AutofillFieldMapping>,
        passwordFields: List<AutofillFieldMapping>,
        focusedField: AutofillFieldMapping?
    ): List<DatasetMapping> = if (focusedField?.autofillFieldId != null) {
        mapWithFocusedField(
            encryptionContext = encryptionContext,
            autofillItem = autofillItem,
            usernameFields = usernameFields,
            passwordFields = passwordFields,
            focusedField = focusedField,
            focusedFieldId = focusedField.autofillFieldId
        )
    } else {
        mapFirstFields(
            encryptionContext = encryptionContext,
            autofillItem = autofillItem,
            usernameFields = usernameFields,
            passwordFields = passwordFields
        )
    }

    @Suppress("LongParameterList")
    private fun mapWithFocusedField(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        usernameFields: List<AutofillFieldMapping>,
        passwordFields: List<AutofillFieldMapping>,
        focusedField: AutofillFieldMapping,
        focusedFieldId: AutofillFieldId
    ): List<DatasetMapping> {
        val mappingList = mutableListOf<DatasetMapping>()

        val focusedFieldResult = mapFocusedField(
            encryptionContext = encryptionContext,
            autofillItem = autofillItem,
            usernameFields = usernameFields,
            passwordFields = passwordFields,
            focusedField = focusedField,
            focusedFieldId = focusedFieldId,
            mappings = mappingList
        )

        when (focusedFieldResult) {
            MapFocusedFieldResult.MappedUsername -> {
                // We have mapped the username, try to map the password
                mapPassword(
                    encryptionContext = encryptionContext,
                    autofillItem = autofillItem,
                    passwordFields = passwordFields,
                    usernameField = focusedField,
                    mappings = mappingList
                )
            }

            MapFocusedFieldResult.MappedPassword -> {
                // We have mapped the password, try to map the username
                mapUsername(
                    autofillItem = autofillItem,
                    usernameFields = usernameFields,
                    passwordField = focusedField,
                    mappings = mappingList
                )
            }

            MapFocusedFieldResult.None -> {
                // We have not been able to map any, map the first fields
                return mapFirstFields(
                    encryptionContext = encryptionContext,
                    autofillItem = autofillItem,
                    usernameFields = usernameFields,
                    passwordFields = passwordFields
                )
            }
        }

        return mappingList
    }

    private fun mapPassword(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        passwordFields: List<AutofillFieldMapping>,
        usernameField: AutofillFieldMapping,
        mappings: MutableList<DatasetMapping>
    ) {
        // If there are no password fields or the autofillItem doesn't have a password, nothing to do
        if (passwordFields.isEmpty() || autofillItem.password == null) return

        // If there is only one password field, map that one
        if (passwordFields.size == 1) {
            val passwordField = passwordFields.first()
            passwordField.autofillFieldId?.let { id ->
                mappings.add(mappingForPassword(encryptionContext, autofillItem.password, id))
            }
            return
        }

        // There is more than one password field. Try to find which one
        val passwordField = findNearestNodeByParentId(usernameField, passwordFields)
        passwordField?.autofillFieldId?.let { id ->
            mappings.add(mappingForPassword(encryptionContext, autofillItem.password, id))
        }
    }

    private fun mapUsername(
        autofillItem: AutofillItem.Login,
        usernameFields: List<AutofillFieldMapping>,
        passwordField: AutofillFieldMapping,
        mappings: MutableList<DatasetMapping>
    ) {
        // If there are no username fields or the autofillItem doesn't have a username, nothing to do
        if (usernameFields.isEmpty() || autofillItem.username.isBlank()) return

        // If there is only one password field, map that one
        if (usernameFields.size == 1) {
            val usernameField = usernameFields.first()
            usernameField.autofillFieldId?.let { id ->
                mappings.add(mappingForUsername(autofillItem.username, id))
            }
            return
        }

        // There is more than one username field. Try to find which one
        val usernameField = findNearestNodeByParentId(passwordField, usernameFields)
        usernameField?.autofillFieldId?.let { id ->
            mappings.add(mappingForUsername(autofillItem.username, id))
        }
    }

    private sealed interface MapFocusedFieldResult {
        object MappedUsername : MapFocusedFieldResult
        object MappedPassword : MapFocusedFieldResult
        object None : MapFocusedFieldResult
    }

    @Suppress("LongParameterList")
    private fun mapFocusedField(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        usernameFields: List<AutofillFieldMapping>,
        passwordFields: List<AutofillFieldMapping>,
        focusedField: AutofillFieldMapping,
        focusedFieldId: AutofillFieldId,
        mappings: MutableList<DatasetMapping>
    ): MapFocusedFieldResult {
        when {
            usernameFields.contains(focusedField) -> {
                mappings.add(
                    DatasetMapping(
                        autofillFieldId = focusedFieldId,
                        contents = autofillItem.username,
                        displayValue = autofillItem.username
                    )
                )
                return MapFocusedFieldResult.MappedUsername
            }

            passwordFields.contains(focusedField) -> {
                if (autofillItem.password != null) {
                    mappings.add(
                        DatasetMapping(
                            autofillFieldId = focusedFieldId,
                            contents = encryptionContext.decrypt(autofillItem.password),
                            displayValue = ""
                        )
                    )
                    return MapFocusedFieldResult.MappedPassword
                }
            }
        }

        return MapFocusedFieldResult.None
    }


    private fun mapFirstFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        usernameFields: List<AutofillFieldMapping>,
        passwordFields: List<AutofillFieldMapping>,
    ): List<DatasetMapping> {
        val mappingList = mutableListOf<DatasetMapping>()

        if (usernameFields.isNotEmpty()) {
            val usernameField = usernameFields.first()
            usernameField.autofillFieldId?.let { id ->
                mappingList.add(
                    DatasetMapping(
                        autofillFieldId = id,
                        contents = autofillItem.username,
                        displayValue = autofillItem.username
                    )
                )
            }
        }

        if (passwordFields.isNotEmpty() && autofillItem.password != null) {
            val passwordField = passwordFields.first()
            passwordField.autofillFieldId?.let { id ->
                mappingList.add(
                    DatasetMapping(
                        autofillFieldId = id,
                        contents = encryptionContext.decrypt(autofillItem.password),
                        displayValue = ""
                    )
                )
            }
        }

        return mappingList
    }

    private fun mappingForPassword(
        encryptionContext: EncryptionContext,
        password: EncryptedString,
        id: AutofillFieldId
    ) = DatasetMapping(
        autofillFieldId = id,
        contents = encryptionContext.decrypt(password),
        displayValue = ""
    )

    private fun mappingForUsername(username: String, id: AutofillFieldId) = DatasetMapping(
        autofillFieldId = id,
        contents = username,
        displayValue = username
    )

    private fun mapToFields(
        androidAutofillFieldIds: List<AutofillFieldId?>,
        autofillTypes: List<FieldType>,
        fieldIsFocusedList: List<Boolean>,
        parentIdList: List<List<AutofillFieldId>>
    ): List<AutofillFieldMapping> = autofillTypes.mapIndexed { index, fieldType ->
        val autofillFieldId = androidAutofillFieldIds.getOrNull(index)
        val isFocused = fieldIsFocusedList.getOrNull(index) ?: false
        val nodePath = parentIdList.getOrNull(index) ?: emptyList()
        AutofillFieldMapping(autofillFieldId, fieldType, isFocused, nodePath)
    }
}

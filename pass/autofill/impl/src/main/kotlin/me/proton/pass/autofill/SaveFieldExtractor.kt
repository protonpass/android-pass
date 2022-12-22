package me.proton.pass.autofill

import me.proton.pass.autofill.entities.AssistField
import me.proton.pass.autofill.entities.FieldType
import me.proton.pass.autofill.entities.SaveInformation
import me.proton.pass.autofill.entities.SaveItemType

object SaveFieldExtractor {
    fun extract(
        fieldsToSave: List<AssistField>,
        packageName: String,
        applicationName: String
    ): List<SaveInformation> {
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
                SaveInformation(applicationName, packageName, SaveItemType.Login(identityValue, passwordValue))
            )
        } else {
            validFields.map {
                SaveInformation(applicationName, packageName, SaveItemType.SingleValue(it.text.toString()))
            }
        }
    }
}

package me.proton.core.pass.autofill.service

import me.proton.core.pass.autofill.service.entities.AssistField
import me.proton.core.pass.autofill.service.entities.SecretSaveInfo
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue

class SecretSaveInfoFetcher {

    fun fetch(
        fieldsToSave: List<AssistField>,
        applicationName: String,
        packageName: String
    ): List<SecretSaveInfo> {
        val isIdentity: (AssistField) -> Boolean = {
            listOf(SecretType.Username, SecretType.Email).contains(it.type)
        }

        val validFields = fieldsToSave.filter { it.type != null }

        val passwordField = validFields.firstOrNull { it.type == SecretType.Password }
        var identityField = validFields.firstOrNull(isIdentity)

        // If we have 2 fields, it's highly likely that the other one will be a username / email
        if (identityField == null && passwordField != null && validFields.count() == 2) {
            identityField = validFields.firstOrNull {
                it !== passwordField && it.type != SecretType.Password
            }
        }

        val identityValue = identityField?.text
        val passwordValue = passwordField?.text

        return if (identityValue != null && passwordValue != null) {
            val loginValue = SecretValue.Login(identityValue, passwordValue)
            listOf(
                SecretSaveInfo(
                    applicationName,
                    packageName,
                    SecretType.Login,
                    loginValue
                )
            )
        } else {
            validFields.map {
                SecretSaveInfo(
                    applicationName,
                    packageName,
                    it.type ?: SecretType.Other,
                    SecretValue.Single(it.text.toString())
                )
            }
        }
    }

}

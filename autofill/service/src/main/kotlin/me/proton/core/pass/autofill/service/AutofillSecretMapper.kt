package me.proton.core.pass.autofill.service

import me.proton.core.pass.autofill.service.entities.AssistField
import me.proton.core.pass.autofill.service.entities.DatasetMapping
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.common_secret.Secret

class AutofillSecretMapper {

    fun mapSecretsToFields(secret: Secret, assistFields: List<AssistField>): List<DatasetMapping> {
        val secretValue = secret.contents
        val validFields = assistFields.filter { it.type != null }
        return if (secretValue is SecretValue.Login) {
            val secrets = mutableMapOf(
                SecretType.Username to secret.copy(
                    type = SecretType.Username,
                    contents = SecretValue.Single(secretValue.identity)
                ),
                SecretType.Email to secret.copy(
                    type = SecretType.Email,
                    contents = SecretValue.Single(secretValue.identity)
                ),
                SecretType.Password to secret.copy(
                    type = SecretType.Password,
                    contents = SecretValue.Single(secretValue.password)
                )
            )

            if (validFields.count() == 2) {
                // Fallback until heuristics are improved for username fields in html
                secrets[SecretType.Other] = secret.copy(
                    type = SecretType.Email,
                    contents = SecretValue.Single(secretValue.identity)
                )
            }

            validFields.mapNotNull { field ->
                val subSecret = secrets[field.type] ?: return@mapNotNull null
                val contents = (subSecret.contents as SecretValue.Single).contents
                val displayValue = getSecretDisplayValue(subSecret)
                DatasetMapping(field.id, contents, displayValue)
            }
        } else {
            val filledInField = validFields.firstOrNull { it.type == secret.type }
            filledInField?.let {
                val contents = (secret.contents as SecretValue.Single).contents
                listOf(DatasetMapping(it.id, contents, getSecretDisplayValue(secret)))
            } ?: emptyList()
        }
    }

    private fun getSecretDisplayValue(secret: Secret): String {
        // Hide value if it's a password
        val contents = when (secret.contents) {
            is SecretValue.Single -> (secret.contents as SecretValue.Single).contents
            else -> ""
        }
        return if (secret.type != SecretType.Password)
            contents else
            secret.name
    }
}

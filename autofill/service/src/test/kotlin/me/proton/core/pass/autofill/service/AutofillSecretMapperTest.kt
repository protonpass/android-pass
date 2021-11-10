package me.proton.core.pass.autofill.service

import me.proton.core.pass.autofill.service.entities.AssistField
import me.proton.core.pass.autofill.service.utils.newAutofillFieldId
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.common_secret.Secret
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AutofillSecretMapperTest {

    private val packageName = "me.proton.core.pass.autofill.service"

    @Test
    fun assistFieldsInConvertMustNotBeEmpty() {
        val secret = Secret(
            null,
            "user_id",
            "address_id",
            "Name",
            SecretType.Email,
            false,
            SecretValue.Single("a@b.com"),
            associatedUris = listOf(packageName)
        )
        val assistFields = emptyList<AssistField>()

        val mappings = AutofillSecretMapper().mapSecretsToFields(secret, assistFields)

        Assert.assertTrue(mappings.isEmpty())
    }

    @Test
    fun secretsWithNoFieldsHavingTheSameTypeCannotBeMapped() {
        val secret = Secret(
            null,
            "user_id",
            "address_id",
            "Name",
            SecretType.Email,
            false,
            SecretValue.Single("a@b.com"),
            associatedUris = listOf(packageName)
        )
        val assistFields = listOf(
            AssistField(newAutofillFieldId(), SecretType.Other, null, null)
        )

        val mappings = AutofillSecretMapper().mapSecretsToFields(secret, assistFields)

        Assert.assertTrue(mappings.isEmpty())
    }

    @Test
    fun loginSecretMapsIdentityToEmailAndPasswordFields() {
        val emailValue = "a@b.com"
        val passwordValue = "pass"
        val secret = Secret(
            null,
            "user_id",
            "address_id",
            "Name",
            SecretType.Login,
            false,
            SecretValue.Login(emailValue, passwordValue),
            associatedUris = listOf(packageName)
        )
        val assistFields = listOf(
            AssistField(newAutofillFieldId(), SecretType.Email, null, null),
            AssistField(newAutofillFieldId(), SecretType.Password, null, null),
        )

        val mappings = AutofillSecretMapper().mapSecretsToFields(secret, assistFields)

        Assert.assertEquals(2, mappings.count())
        Assert.assertEquals(assistFields.first().id, mappings.firstOrNull()?.autofillFieldId)
        Assert.assertEquals(emailValue, mappings.firstOrNull()?.contents)
        Assert.assertEquals(assistFields.last().id, mappings.lastOrNull()?.autofillFieldId)
        Assert.assertEquals(passwordValue, mappings.lastOrNull()?.contents)
    }

    @Test
    fun loginSecretMapsIdentityToUsernameAndPasswordFields() {
        val usernameValue = "SomeUsername"
        val passwordValue = "pass"
        val secret = Secret(
            null,
            "user_id",
            "address_id",
            "Name",
            SecretType.Login,
            false,
            SecretValue.Login(usernameValue, passwordValue),
            associatedUris = listOf(packageName)
        )
        val assistFields = listOf(
            AssistField(newAutofillFieldId(), SecretType.Username, null, null),
            AssistField(newAutofillFieldId(), SecretType.Password, null, null),
        )

        val mappings = AutofillSecretMapper().mapSecretsToFields(secret, assistFields)

        Assert.assertEquals(2, mappings.count())
        Assert.assertEquals(assistFields.first().id, mappings.firstOrNull()?.autofillFieldId)
        Assert.assertEquals(usernameValue, mappings.firstOrNull()?.contents)
        Assert.assertEquals(assistFields.last().id, mappings.lastOrNull()?.autofillFieldId)
        Assert.assertEquals(passwordValue, mappings.lastOrNull()?.contents)
    }

    @Test
    fun singleValueSecretIsMappedToCompatibleField() {
        val usernameValue = "SomeUsername"
        val secret = Secret(
            null,
            "user_id",
            "address_id",
            "Name",
            SecretType.Username,
            false,
            SecretValue.Single(usernameValue),
            associatedUris = listOf(packageName)
        )
        val compatibleField = AssistField(
            newAutofillFieldId(),
            SecretType.Username,
            null,
            null
        )
        val assistFields = listOf(
            AssistField(newAutofillFieldId(), SecretType.Email, null, null),
            compatibleField,
            AssistField(newAutofillFieldId(), SecretType.Password, null, null),
        )

        val mappings = AutofillSecretMapper().mapSecretsToFields(secret, assistFields)

        Assert.assertEquals(1, mappings.count())
        Assert.assertEquals(compatibleField.id, mappings.firstOrNull()?.autofillFieldId)
        Assert.assertEquals(usernameValue, mappings.firstOrNull()?.contents)
    }

}

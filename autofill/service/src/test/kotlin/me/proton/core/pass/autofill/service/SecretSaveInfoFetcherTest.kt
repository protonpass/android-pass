package me.proton.core.pass.autofill.service

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.pass.autofill.service.entities.AssistField
import me.proton.core.pass.autofill.service.utils.newAutofillFieldId
import me.proton.core.pass.commonsecret.SecretType
import me.proton.core.pass.commonsecret.SecretValue
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SecretSaveInfoFetcherTest {

    @Test
    fun fieldsWithNoSecretTypeAreFiltered() = runBlockingTest {
        val invalid = AssistField(newAutofillFieldId(), null, null, "Text")
        val valid = AssistField(newAutofillFieldId(), SecretType.Email, null, "a@b.com")

        val results = SecretSaveInfoFetcher().fetch(
            listOf(invalid, valid),
            "Some app",
            "a.b.c"
        )

        Assert.assertEquals(1, results.count())
        Assert.assertEquals(valid.type, results.firstOrNull()?.secretType)
    }

    @Test
    fun loginSecretIsCreatedIfUsernameAndPasswordArePresent() = runBlockingTest {
        val username = AssistField(newAutofillFieldId(), SecretType.Username, null, "user")
        val password = AssistField(newAutofillFieldId(), SecretType.Password, null, "pass")

        val results = SecretSaveInfoFetcher().fetch(
            listOf(username, password),
            "Some app",
            "a.b.c"
        )

        Assert.assertEquals(1, results.count())
        Assert.assertTrue(results.firstOrNull()?.secretValue is SecretValue.Login)
    }

    @Test
    fun loginSecretIsCreatedIfEmailAndPasswordArePresent() = runBlockingTest {
        val email = AssistField(newAutofillFieldId(), SecretType.Email, null, "a@b.com")
        val password = AssistField(newAutofillFieldId(), SecretType.Password, null, "pass")

        val results = SecretSaveInfoFetcher().fetch(
            listOf(email, password),
            "Some app",
            "a.b.c"
        )

        Assert.assertEquals(1, results.count())
        Assert.assertTrue(results.firstOrNull()?.secretValue is SecretValue.Login)
    }

    @Test
    fun loginSecretIsCreatedIfOnlyGenericTextAndPasswordArePresent() = runBlocking {
        val text = AssistField(newAutofillFieldId(), SecretType.Other, null, "maybeUser")
        val password = AssistField(newAutofillFieldId(), SecretType.Password, null, "pass")

        val results = SecretSaveInfoFetcher().fetch(
            listOf(text, password),
            "Some app",
            "a.b.c"
        )

        Assert.assertEquals(1, results.count())
        Assert.assertTrue(results.firstOrNull()?.secretValue is SecretValue.Login)
    }

    @Test
    fun loginSecretIsNotCreatedIfSeveralPasswordFieldsArePresent() = runBlocking {
        val password = AssistField(newAutofillFieldId(), SecretType.Password, null, "pass")
        val password2 = AssistField(newAutofillFieldId(), SecretType.Password, null, "pass2")

        val results = SecretSaveInfoFetcher().fetch(
            listOf(password, password2),
            "Some app",
            "a.b.c"
        )

        Assert.assertEquals(2, results.count())
        Assert.assertTrue(results.all { it.secretValue is SecretValue.Single })
    }

    @Test
    fun ifCredentialsAreNotLoginAllOfThemArePersistedIndividually() = runBlockingTest {
        val username = AssistField(newAutofillFieldId(), SecretType.Username, null, "user")
        val email = AssistField(newAutofillFieldId(), SecretType.Email, null, "a@b.com")
        val text = AssistField(newAutofillFieldId(), SecretType.Other, null, "maybeUser")

        val results = SecretSaveInfoFetcher().fetch(
            listOf(username, email, text),
            "Some app",
            "a.b.c"
        )

        Assert.assertEquals(3, results.count())
        Assert.assertTrue(results.all { it.secretValue is SecretValue.Single })
    }

}

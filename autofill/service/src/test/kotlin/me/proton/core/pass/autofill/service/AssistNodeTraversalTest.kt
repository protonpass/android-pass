package me.proton.core.pass.autofill.service

import android.text.InputType
import android.view.View
import me.proton.core.pass.autofill.service.entities.AutofillFieldId
import me.proton.core.pass.autofill.service.entities.AutofillNode
import me.proton.core.pass.autofill.service.utils.newAutofillFieldId
import me.proton.core.pass.commonsecret.SecretType
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AssistNodeTraversalTest {

    @Test
    fun allNodesAreVisited() {
        // Mix of valid nodes and invalid ones
        val rootNode = makeValidNode(
            children = listOf(
                makeNode(
                    children = listOf(makeNode(), makeValidNode())
                ),
                makeValidNode()
            )
        )

        val traversal = AssistNodeTraversal()
        traversal.traverse(rootNode)

        Assert.assertEquals(5, traversal.visitedNodes)
    }

    @Test
    fun canExtractFieldFromHints() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            autofillHints = listOf(View.AUTOFILL_HINT_EMAIL_ADDRESS),
            isImportantForAutofill = true
        )

        val result = AssistNodeTraversal().traverse(rootNode)

        Assert.assertEquals(SecretType.Email, result.firstOrNull()?.type)
    }

    @Test
    fun canExtractFieldFromInputType() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            isImportantForAutofill = true
        )

        val result = AssistNodeTraversal().traverse(rootNode)

        Assert.assertEquals(SecretType.Email, result.firstOrNull()?.type)
    }

    @Test
    fun canExtractFieldFromHtmlAttributes() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            htmlAttributes = listOf("type" to "text"),
            isImportantForAutofill = true
        )

        val result = AssistNodeTraversal().traverse(rootNode)

        Assert.assertEquals(SecretType.Other, result.firstOrNull()?.type)
    }

    @Test
    fun autofillHintsParsingReturnsKnownSecretType() {
        val traversal = AssistNodeTraversal()

        val phoneType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_PHONE)
        val usernameType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_USERNAME)
        val emailType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        val passwordType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_PASSWORD)
        val nameType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_NAME)
        val creditCardType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_CREDIT_CARD_NUMBER)

        Assert.assertEquals(SecretType.Phone, phoneType)
        Assert.assertEquals(SecretType.Username, usernameType)
        Assert.assertEquals(SecretType.Email, emailType)
        Assert.assertEquals(SecretType.Password, passwordType)
        Assert.assertEquals(SecretType.FullName, nameType)
        // Still not supported
        Assert.assertNull(creditCardType)
    }

    @Test
    fun inputTypeParsingReturnsKnownSecretType() {
        val traversal = AssistNodeTraversal()

        val phoneType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_CLASS_PHONE)
        val emailType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val webEmailType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
        val passwordType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val numPasswordType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        val webPasswordType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
        val visiblePasswordType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        val nameType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        val longMessageType = traversal.detectFieldTypeUsingInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)

        Assert.assertEquals(SecretType.Phone, phoneType)
        Assert.assertEquals(SecretType.Email, emailType)
        Assert.assertEquals(SecretType.Email, webEmailType)
        Assert.assertEquals(SecretType.Password, passwordType)
        Assert.assertEquals(SecretType.Password, numPasswordType)
        Assert.assertEquals(SecretType.Password, webPasswordType)
        Assert.assertEquals(SecretType.Password, visiblePasswordType)
        Assert.assertEquals(SecretType.FullName, nameType)
        // Not supported
        Assert.assertNull(longMessageType)
    }

    @Test
    fun htmlAttributesParsingReturnsKnownSecretType() {
        val traversal = AssistNodeTraversal()

        val phoneType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "tel"))
        val emailType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "email"))
        val passwordType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "password"))
        val genericType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "text"))
        val buttonType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "button"))

        Assert.assertEquals(SecretType.Phone, phoneType)
        Assert.assertEquals(SecretType.Email, emailType)
        Assert.assertEquals(SecretType.Password, passwordType)
        Assert.assertEquals(SecretType.Other, genericType)
        // Not supported
        Assert.assertNull(buttonType)
    }

    private fun makeValidNode(children: List<AutofillNode> = emptyList()) =
        makeNode(
            autofillId = newAutofillFieldId(),
            text = "a@b.com",
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            children = children
        )

    private fun makeNode(
        autofillId: AutofillFieldId? = null,
        className: String? = null,
        isImportantForAutofill: Boolean = false,
        text: String? = null,
        inputType: Int = 0,
        autofillHints: List<String> = emptyList(),
        htmlAttributes: List<Pair<String, String>> = emptyList(),
        children: List<AutofillNode> = emptyList()
    ) =
        AutofillNode(
            id = autofillId,
            className = className,
            isImportantForAutofill = isImportantForAutofill,
            text = text,
            autofillValue = null,
            inputType = inputType,
            autofillHints = autofillHints,
            htmlAttributes = htmlAttributes,
            children
        )

}

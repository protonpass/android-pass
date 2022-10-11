package me.proton.core.pass.autofill.service

import android.text.InputType
import android.view.View
import me.proton.core.pass.autofill.AssistNodeTraversal
import me.proton.core.pass.autofill.entities.AutofillFieldId
import me.proton.core.pass.autofill.entities.AutofillNode
import me.proton.core.pass.autofill.entities.FieldType
import me.proton.core.pass.autofill.entities.InputTypeValue
import me.proton.core.pass.autofill.service.utils.newAutofillFieldId
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

/*    @Test
    fun canExtractFieldFromHints() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            autofillHints = listOf(View.AUTOFILL_HINT_EMAIL_ADDRESS),
            isImportantForAutofill = true
        )

        val result = AssistNodeTraversal().traverse(rootNode)

        Assert.assertEquals(FieldType.Email, result.firstOrNull()?.type)
    }*/

    @Test
    fun canExtractFieldFromInputType() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            isImportantForAutofill = true
        )

        val result = AssistNodeTraversal().traverse(rootNode)

        Assert.assertEquals(FieldType.Email, result.firstOrNull()?.type)
    }

/*
    @Test
    fun canExtractFieldFromHtmlAttributes() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            htmlAttributes = listOf("type" to "text"),
            isImportantForAutofill = true
        )

        val result = AssistNodeTraversal().traverse(rootNode)

        Assert.assertEquals(FieldType.Other, result.firstOrNull()?.type)
    }
*/

    @Test
    fun autofillHintsParsingReturnsKnownFieldType() {
        val traversal = AssistNodeTraversal()

        val phoneType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_PHONE)
        val usernameType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_USERNAME)
        val emailType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        val passwordType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_PASSWORD)
        val nameType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_NAME)
        val creditCardType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_CREDIT_CARD_NUMBER)

        Assert.assertEquals(FieldType.Phone, phoneType)
        Assert.assertEquals(FieldType.Username, usernameType)
        Assert.assertEquals(FieldType.Email, emailType)
        Assert.assertEquals(FieldType.Password, passwordType)
        Assert.assertEquals(FieldType.FullName, nameType)
        // Still not supported
        Assert.assertEquals(FieldType.Unknown, creditCardType)
    }

/*    @Test
    fun inputTypeParsingReturnsKnownFieldType() {
        val traversal = AssistNodeTraversal()

        val fieldWithInputType = { inputType: Int ->
            traversal.detectFieldTypeUsingInputType(InputTypeValue(inputType))
        }

        val phoneType = fieldWithInputType(InputType.TYPE_CLASS_PHONE)
        val emailType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val webEmailType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)
        val passwordType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val numPasswordType = fieldWithInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        val webPasswordType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
        val visiblePasswordType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
        val nameType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
        val longMessageType = fieldWithInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)

        Assert.assertEquals(FieldType.Phone, phoneType)
        Assert.assertEquals(FieldType.Email, emailType)
        Assert.assertEquals(FieldType.Email, webEmailType)
        Assert.assertEquals(FieldType.Password, passwordType)
        Assert.assertEquals(FieldType.Password, numPasswordType)
        Assert.assertEquals(FieldType.Password, webPasswordType)
        Assert.assertEquals(FieldType.Password, visiblePasswordType)
        Assert.assertEquals(FieldType.FullName, nameType)
        // Not supported
        Assert.assertEquals(FieldType.Unknown, longMessageType)
    }*/

    @Test
    fun htmlAttributesParsingReturnsKnownFieldType() {
        val traversal = AssistNodeTraversal()

        val phoneType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "tel"))
        val emailType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "email"))
        val passwordType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "password"))
        val genericType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "text"))
        val buttonType = traversal.detectFieldTypeUsingHtmlInfo(listOf("type" to "button"))

        Assert.assertEquals(FieldType.Phone, phoneType)
        Assert.assertEquals(FieldType.Email, emailType)
        Assert.assertEquals(FieldType.Password, passwordType)
        Assert.assertEquals(FieldType.Other, genericType)
        // Not supported
        Assert.assertEquals(FieldType.Unknown, buttonType)
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
            inputType = InputTypeValue(inputType),
            autofillHints = autofillHints,
            htmlAttributes = htmlAttributes,
            children
        )

}

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

package proton.android.pass.autofill.service

import android.text.InputType
import android.view.View
import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.InputTypeValue
import proton.android.pass.autofill.heuristics.NodeExtractor
import proton.android.pass.autofill.newAutofillFieldId
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption

@RunWith(JUnit4::class)
class NodeExtractorTest {

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

        val traversal = NodeExtractor()
        traversal.extract(rootNode)

        Assert.assertEquals(5, traversal.visitedNodes)
    }

    @Test
    fun canExtractFieldFromInputType() {
        val rootNode = makeNode(
            autofillId = newAutofillFieldId(),
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            isImportantForAutofill = true
        )

        val result = NodeExtractor().extract(rootNode)

        Assert.assertEquals(FieldType.Email, result.fields.firstOrNull()?.type)
    }

    @Test
    fun autofillHintsParsingReturnsKnownFieldType() {
        val traversal = NodeExtractor()

        val usernameType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_USERNAME)
        val emailType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_EMAIL_ADDRESS)
        val passwordType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_PASSWORD)
        val creditCardType =
            traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_CREDIT_CARD_NUMBER)
        val phoneType = traversal.detectFieldTypeUsingAutofillHint(View.AUTOFILL_HINT_PHONE)

        Assert.assertEquals(FieldType.Username, usernameType)
        Assert.assertEquals(FieldType.Email, emailType)
        Assert.assertEquals(FieldType.Password, passwordType)
        Assert.assertEquals(FieldType.CardNumber, creditCardType)
        Assert.assertEquals(FieldType.Phone, phoneType)
    }

    @Test
    fun `is able to extract URL`() {
        val domain = "somedomain.example"
        val structure = makeNode(
            children = listOf(makeExtractableNode(webDomain = domain))
        )
        val result = NodeExtractor().extract(structure)
        assertThat(result.fields.size).isEqualTo(1)

        val extractedNode = result.fields.first()
        assertThat(extractedNode.url).isEqualTo(domain)
    }

    @Test
    fun `return the right url for each node`() {
        val domain1 = "somedomain.example"
        val domain2 = "other.example"
        val structure = makeNode(
            webDomain = "root.example",
            children = listOf(
                makeExtractableNode(webDomain = domain1),
                makeExtractableNode(webDomain = domain2)
            )
        )
        val result = NodeExtractor().extract(structure)
        assertThat(result.fields.size).isEqualTo(2)

        val extractedNode1 = result.fields[0]
        assertThat(extractedNode1.url).isEqualTo(domain1)

        val extractedNode2 = result.fields[1]
        assertThat(extractedNode2.url).isEqualTo(domain2)
    }

    @Test
    fun `return the child URL if the parent also contains a URL`() {
        val domain1 = "somedomain.example"
        val domain2 = "other.example"
        val structure = makeNode(
            webDomain = domain1,
            children = listOf(makeExtractableNode(webDomain = domain2))
        )
        val result = NodeExtractor().extract(structure)
        assertThat(result.fields.size).isEqualTo(1)

        val extractedNode = result.fields.first()
        assertThat(extractedNode.url).isEqualTo(domain2)
    }

    @Test
    fun `return the parent URL if the current node does not contain a URL`() {
        val domain = "somedomain.example"
        val nodeToExtract = makeExtractableNode()
        val structure = makeNode(
            webDomain = domain,
            children = listOf(nodeToExtract)
        )
        val result = NodeExtractor().extract(structure)
        assertThat(result.fields.size).isEqualTo(1)

        val extractedNode = result.fields.first()
        assertThat(extractedNode.url).isEqualTo(domain)
        assertThat(extractedNode.id).isEqualTo(nodeToExtract.id)
    }

    @Test
    fun `can return the found urls`() {
        val domain1 = "somedomain.example"
        val domain2 = "other.example"
        val structure = makeNode(
            webDomain = "root.example",
            children = listOf(
                makeExtractableNode(webDomain = domain1),
                makeExtractableNode(webDomain = domain2)
            )
        )
        val result = NodeExtractor().extract(structure)
        assertThat(result.urls()).isEqualTo(listOf(domain1, domain2))
    }

    @Test
    fun `can return the selected url`() {
        val domain1 = "somedomain.example"
        val domain2 = "other.example"
        val structure = makeNode(
            webDomain = "root.example",
            children = listOf(
                makeExtractableNode(webDomain = domain1, isFocused = true),
                makeExtractableNode(webDomain = domain2)
            )
        )
        val result = NodeExtractor().extract(structure)
        assertThat(result.mainUrl()).isEqualTo(domain1.some())
    }

    private fun makeValidNode(children: List<AutofillNode> = emptyList()) = makeNode(
        text = "a@b.com",
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        children = children
    )

    private fun makeNode(
        autofillId: AutofillFieldId? = newAutofillFieldId(),
        className: String? = null,
        isImportantForAutofill: Boolean = true,
        text: String? = null,
        inputType: Int = 0,
        autofillHints: List<String> = emptyList(),
        htmlAttributes: List<Pair<String, String>> = emptyList(),
        children: List<AutofillNode> = emptyList(),
        webDomain: String? = null
    ) = AutofillNode(
        id = autofillId,
        className = className,
        isImportantForAutofill = isImportantForAutofill,
        text = text,
        autofillValue = null,
        inputType = InputTypeValue(inputType),
        autofillHints = autofillHints,
        htmlAttributes = htmlAttributes,
        children = children,
        url = webDomain.toOption(),
        hintKeywordList = emptyList(),
        isFocused = false
    )

    private fun makeExtractableNode(
        autofillId: AutofillFieldId? = newAutofillFieldId(),
        text: String? = null,
        autofillHints: List<String> = emptyList(),
        htmlAttributes: List<Pair<String, String>> = emptyList(),
        children: List<AutofillNode> = emptyList(),
        webDomain: String? = null,
        isFocused: Boolean = false
    ) = AutofillNode(
        id = autofillId,
        className = "android.widget.EditText",
        isImportantForAutofill = true,
        text = text,
        autofillValue = null,
        inputType = InputTypeValue(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
        autofillHints = autofillHints,
        htmlAttributes = htmlAttributes,
        children = children,
        url = webDomain.toOption(),
        hintKeywordList = emptyList(),
        isFocused = isFocused
    )
}

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

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import proton.android.pass.autofill.debug.AutofillDebugSaver
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillNode
import proton.android.pass.autofill.heuristics.ItemFieldMapper
import proton.android.pass.autofill.heuristics.NodeClusterer
import proton.android.pass.autofill.heuristics.NodeExtractor
import proton.android.pass.autofill.heuristics.focused
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.log.api.PassLogger
import java.io.File

const val CC_EXPIRATION_YEAR = "2025"
const val CC_EXPIRATION_MONTH = "12"
const val CC_EXPIRATION = "$CC_EXPIRATION_YEAR-$CC_EXPIRATION_MONTH"

enum class ExpectedAutofill(val value: String, val assertedValue: String = value) {
    USERNAME("username"),
    PASSWORD("password"),
    CC_NUMBER("card_number"),
    CC_CARDHOLDER_NAME("card_name", "card_first_name card_last_name"),
    CC_CARDHOLDER_FIRST_NAME("card_first_name"),
    CC_CARDHOLDER_LAST_NAME("card_last_name"),
    CC_EXPIRATION_MM_YY(
        "card_expiration_mm_yy",
        "$CC_EXPIRATION_MONTH/${CC_EXPIRATION_YEAR.takeLast(2)}"
    ),
    CC_EXPIRATION_MONTH_TEXT("card_expiration_month_text"),
    CC_EXPIRATION_MONTH_MM("card_expiration_month_mm", CC_EXPIRATION_MONTH),
    CC_EXPIRATION_YEAR_YY("card_expiration_year_yy", CC_EXPIRATION_YEAR.takeLast(2)),
    CC_EXPIRATION_YEAR_YYYY("card_expiration_year_yyyy", CC_EXPIRATION_YEAR),
    CC_CVV("card_cvv");

    companion object {
        fun all(): List<String> = values().map { it.value }
    }
}

private const val TAG = "RunAutofillTest"

fun runAutofillTest(
    file: String,
    item: AutofillItem,
    requestFlags: List<RequestFlags> = emptyList()
) {
    val path = "src/test/resources/$file"
    val asFile = File(path)
    val content = asFile.readText()
    val parsed: AutofillDebugSaver.DebugAutofillEntry = Json.decodeFromString(content)
    val nodesWithExpectedContents = getExpectedContents(parsed)

    val asAutofillNodes = parsed.rootContent.toAutofillNode()
    val detectedNodes = NodeExtractor(requestFlags).extract(asAutofillNodes)
    val clusters = NodeClusterer.cluster(detectedNodes.fields)
    val focusedCluster = clusters.focused()

    val res = ItemFieldMapper.mapFields(
        encryptionContext = TestEncryptionContext,
        autofillItem = item,
        cluster = focusedCluster
    )

    PassLogger.i(
        TAG,
        "Expected nodes: ${nodesWithExpectedContents.size} (${nodesWithExpectedContents.map { it.second.name }})"
    )
    PassLogger.i(TAG, "Detected nodes: ${detectedNodes.fields.size} (${detectedNodes.fields.map { it.type?.name }})")
    PassLogger.i(TAG, "Mapped nodes: ${res.mappings.size}")
    PassLogger.i(TAG, "Clusters: ${clusters.size} | Focused: $focusedCluster")

    assertThat(res.mappings.size).isEqualTo(nodesWithExpectedContents.size)
    for (nodeWithExpectedContents in nodesWithExpectedContents) {
        val (node, expectedAutofill) = nodeWithExpectedContents
        val field = res.mappings.find {
            (it.autofillFieldId as TestAutofillId).id == node.id
        }
        assertThat(field).isNotNull()

        assertThat(field!!.contents).isEqualTo(expectedAutofill.assertedValue)
    }
}

private fun getExpectedContents(
    entry: AutofillDebugSaver.DebugAutofillEntry
): List<Pair<AutofillDebugSaver.DebugAutofillNode, ExpectedAutofill>> {
    val withContents = mutableListOf<Pair<AutofillDebugSaver.DebugAutofillNode, ExpectedAutofill>>()
    getExpectedContents(entry.rootContent, withContents)
    if (withContents.isEmpty()) {
        throw IllegalStateException("There are no fields with 'expectedAutofill'")
    }
    return withContents
}

private fun getExpectedContents(
    node: AutofillDebugSaver.DebugAutofillNode,
    withExpectedContents: MutableList<Pair<AutofillDebugSaver.DebugAutofillNode, ExpectedAutofill>>
) {
    val expectedContents = node.expectedAutofill
    if (expectedContents != null) {

        val expectedAutofill = ExpectedAutofill.values()
            .firstOrNull { it.value == expectedContents }
            ?: throw IllegalStateException(
                "Unknown expectedAutofill: $expectedContents. Must be one of ${ExpectedAutofill.all()}"
            )

        withExpectedContents.add(node to expectedAutofill)
    }
    node.children.forEach { getExpectedContents(it, withExpectedContents) }
}

fun AutofillDebugSaver.DebugAutofillNode.toAutofillNode(): AutofillNode = AutofillNode(
    className = className,
    isImportantForAutofill = isImportantForAutofill,
    text = text,
    isFocused = isFocused,
    inputType = inputType,
    autofillHints = autofillHints,
    htmlAttributes = htmlAttributes.map { it.key to it.value },
    children = children.map { it.toAutofillNode() },
    url = url.toOption(),
    hintKeywordList = hintKeywordList,
    autofillValue = null,
    id = TestAutofillId(id)
)

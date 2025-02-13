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
import proton.android.pass.autofill.TestUtils.toAutofillNode
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.heuristics.ItemFieldMapper
import proton.android.pass.autofill.heuristics.NodeClusterer
import proton.android.pass.autofill.heuristics.NodeExtractor
import proton.android.pass.autofill.heuristics.focused
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.log.api.PassLogger

const val CC_EXPIRATION_YEAR = "2025"
const val CC_EXPIRATION_MONTH = "12"
const val CC_EXPIRATION = "$CC_EXPIRATION_YEAR-$CC_EXPIRATION_MONTH"

enum class ExpectedAutofill(val value: String, val assertedValue: String = value) {
    USERNAME("username"),
    EMAIL("email"),
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
    CC_CVV("card_cvv"),

    IDENTITY_FULL_NAME("identity_full_name"),
    IDENTITY_FIRST_NAME("identity_first_name"),
    IDENTITY_MIDDLE_NAME("identity_middle_name"),
    IDENTITY_LAST_NAME("identity_last_name"),
    IDENTITY_ADDRESS("identity_address"),
    IDENTITY_CITY("identity_city"),
    IDENTITY_POSTAL_CODE("identity_postal_code"),
    IDENTITY_PHONE("identity_phone"),
    IDENTITY_ORGANIZATION("identity_organization"),
    IDENTITY_COUNTRY("identity_country")

    ;

    companion object {
        fun all(): List<String> = entries.map { it.value }
    }
}

private const val TAG = "RunAutofillTest"

fun runAutofillTest(
    file: String,
    item: AutofillItem,
    requestFlags: List<RequestFlags> = emptyList(),
    allowEmptyFields: Boolean = false
) {
    val parsed = TestUtils.parseResourceFile(file)
    val nodesWithExpectedContents = TestUtils.getExpectedContents(parsed, allowEmptyFields)
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
        "Expected nodes: ${nodesWithExpectedContents.size} " +
            "(${nodesWithExpectedContents.map { it.second.name + ":" + it.first.id }})"
    )
    PassLogger.i(
        TAG,
        "Detected nodes: ${detectedNodes.fields.size} " +
            "(${detectedNodes.fields.map { it.type?.name + ":" + it.id.value() }})"
    )
    val notFound = nodesWithExpectedContents.filter { (node, _) ->
        res.mappings.none { (it.autofillFieldId as TestAutofillId).id == node.id }
    }
    PassLogger.i(
        TAG,
        "Expected nodes not detected: " +
            "${notFound.size} (${notFound.map { it.second.name + ":" + it.first.id }})"
    )
    PassLogger.i(TAG, "Mapped nodes: ${res.mappings.size}")
    PassLogger.i(TAG, "Clusters: ${clusters.size}")
    PassLogger.i(TAG, "Focused cluster type: ${focusedCluster.type()}")
    PassLogger.i(TAG, "Focused cluster fields: ${focusedCluster.fields().map { it.type?.name + ":" + it.id.value() }}")

    for (nodeWithExpectedContents in nodesWithExpectedContents) {
        val (node, expectedAutofill) = nodeWithExpectedContents
        val field = res.mappings.find {
            (it.autofillFieldId as TestAutofillId).id == node.id
        }
        assertThat(field?.contents).isEqualTo(expectedAutofill.assertedValue)
    }
    assertThat(res.mappings.size).isEqualTo(nodesWithExpectedContents.size)
}

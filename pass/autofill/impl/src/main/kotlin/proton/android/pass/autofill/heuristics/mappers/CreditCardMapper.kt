/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.autofill.heuristics.mappers

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.crypto.api.context.EncryptionContext

object CreditCardMapper {

    fun mapCreditCardFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.CreditCard,
        cluster: NodeCluster.CreditCard
    ): AutofillMappings {
        val mappings = mutableListOf<DatasetMapping>()
        mappings.add(mappingForCardNumber(autofillItem.number, cluster.cardNumber.id))

        if (autofillItem.cardHolder.isNotBlank()) {
            cluster.cardHolder?.let {
                mappings.addAll(mappingsForCardHolder(autofillItem.cardHolder, it))
            }
        }

        cluster.cvv?.let {
            mappings.add(mappingForCvv(encryptionContext, autofillItem.cvv, it.id))
        }
        cluster.expiration?.let {
            mappings.addAll(mappingsForExpiration(autofillItem.expiration, it))
        }

        return AutofillMappings(mappings)
    }

    private fun mappingsForCardHolder(
        cardHolder: String,
        cluster: NodeCluster.CreditCard.CardHolder
    ): List<DatasetMapping> {
        val splits = cardHolder.split(" ")
        return when (cluster) {
            is NodeCluster.CreditCard.CardHolder.FirstNameLastName -> when {
                splits.size == 2 -> listOf(
                    mappingForCardHolder(splits[0], cluster.firstName.id),
                    mappingForCardHolder(splits[1], cluster.lastName.id)
                )
                splits.size > 2 -> listOf(
                    mappingForCardHolder(splits[0], cluster.firstName.id),
                    mappingForCardHolder(splits.drop(1).joinToString(" "), cluster.lastName.id)
                )
                else -> listOf(
                    mappingForCardHolder(cardHolder, cluster.firstName.id),
                    mappingForCardHolder("", cluster.lastName.id)
                )
            }
            is NodeCluster.CreditCard.CardHolder.SingleField ->
                listOf(mappingForCardHolder(cardHolder, cluster.field.id))
        }
    }

    private fun mappingsForExpiration(
        expiration: String,
        cluster: NodeCluster.CreditCard.Expiration
    ): List<DatasetMapping> {
        val splits = expiration.split("-")
        if (splits.size != 2) return emptyList()
        val fullYear = splits[0]
        val shortYear = fullYear.takeLast(2)
        val month = splits[1]
        return when (cluster) {
            is NodeCluster.CreditCard.Expiration.MmYyDifferentfields -> listOf(
                mappingForExpiration(shortYear, cluster.year.id, findListIndex(shortYear, cluster.year.listOptions)),
                mappingForExpiration(month, cluster.month.id, findListIndex(month, cluster.month.listOptions))
            )
            is NodeCluster.CreditCard.Expiration.MmYyyyDifferentfields -> listOf(
                mappingForExpiration(fullYear, cluster.year.id, findListIndex(fullYear, cluster.year.listOptions)),
                mappingForExpiration(month, cluster.month.id, findListIndex(month, cluster.month.listOptions))
            )
            is NodeCluster.CreditCard.Expiration.MmYySameField -> {
                val mmYy = "$month/$shortYear"
                val mmYyyy = "$month/$fullYear"
                val listIndex = findListIndex(mmYy, cluster.field.listOptions)
                    ?: findListIndex(mmYyyy, cluster.field.listOptions)
                listOf(mappingForExpiration(mmYy, cluster.field.id, listIndex))
            }
        }
    }

    @Suppress("MagicNumber")
    private fun mappingForCardNumber(cardNumber: String, id: AutofillFieldId) = DatasetMapping(
        autofillFieldId = id,
        contents = cardNumber,
        displayValue = "•••• ${cardNumber.takeLast(4)}"
    )

    private fun mappingForCardHolder(cardHolder: String, id: AutofillFieldId) = DatasetMapping(
        autofillFieldId = id,
        contents = cardHolder,
        displayValue = ""
    )

    private fun mappingForCvv(
        encryptionContext: EncryptionContext,
        cvv: EncryptedString?,
        id: AutofillFieldId
    ) = DatasetMapping(
        autofillFieldId = id,
        contents = cvv?.let { encryptionContext.decrypt(cvv) } ?: "",
        displayValue = ""
    )

    private fun mappingForExpiration(
        expiration: String,
        id: AutofillFieldId,
        listIndex: Int? = null
    ) = DatasetMapping(
        autofillFieldId = id,
        contents = expiration,
        displayValue = "",
        listIndex = listIndex
    )

    private fun findListIndex(value: String, options: List<String>): Int? {
        if (options.isEmpty()) return null
        // Android maps AutofillValue.forList(index) to non-blank options only
        val nonEmptyOptions = options.filter { it.isNotBlank() }
        val exactIndex = nonEmptyOptions.indexOfFirst { it == value }
        if (exactIndex >= 0) return exactIndex
        // For 2-digit year values, try matching against 4-digit year options (e.g. "25" → "2025")
        if (value.length == 2) {
            val expandedYear = "20$value"
            val expandedIndex = nonEmptyOptions.indexOfFirst { it == expandedYear }
            if (expandedIndex >= 0) return expandedIndex
        }
        return null
    }

}

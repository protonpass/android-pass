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
        val decryptedNumber = encryptionContext.decrypt(autofillItem.number)
        mappings.add(mappingForCardNumber(decryptedNumber, cluster.cardNumber.id))

        cluster.cardHolder?.let {
            mappings.add(mappingForCardHolder(autofillItem.cardHolder, it.id))
        }
        cluster.cvv?.let {
            mappings.add(mappingForCvv(encryptionContext, autofillItem.cvv, it.id))
        }
        cluster.expiration?.let {
            val expirationSplits = autofillItem.expiration.split("-")
            if (expirationSplits.size == 2) {
                val expirationFullYear = expirationSplits[0]
                val expirationYearLast = expirationFullYear.takeLast(2)
                val expirationMonth = expirationSplits[1]
                when (it) {
                    is NodeCluster.CreditCard.Expiration.MmYyDifferentfields -> {
                        mappings.add(mappingForExpiration(expirationYearLast, it.year.id))
                        mappings.add(mappingForExpiration(expirationMonth, it.month.id))
                    }

                    is NodeCluster.CreditCard.Expiration.MmYyyyDifferentfields -> {
                        mappings.add(mappingForExpiration(expirationFullYear, it.year.id))
                        mappings.add(mappingForExpiration(expirationMonth, it.month.id))
                    }

                    is NodeCluster.CreditCard.Expiration.MmYySameField -> {
                        val expiration = "$expirationMonth/$expirationYearLast"
                        mappings.add(mappingForExpiration(expiration, it.field.id))
                    }
                }
            }
        }

        return AutofillMappings(mappings)
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

    private fun mappingForExpiration(expiration: String, id: AutofillFieldId) = DatasetMapping(
        autofillFieldId = id,
        contents = expiration,
        displayValue = ""
    )

}

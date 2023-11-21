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

package proton.android.pass.autofill.heuristics

import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.heuristics.mappers.CreditCardMapper.mapCreditCardFields
import proton.android.pass.autofill.heuristics.mappers.LoginMapper.mapLoginFields
import proton.android.pass.autofill.heuristics.mappers.LoginMapper.mapSignUpFields
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.log.api.PassLogger

object ItemFieldMapper {

    private const val TAG = "ItemFieldMapper"

    @Suppress("ReturnCount")
    fun mapFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem,
        cluster: NodeCluster
    ): AutofillMappings {
        when (cluster) {
            NodeCluster.Empty -> {
                PassLogger.e(TAG, "Expected NodeCluster to be non-empty")
            }

            is NodeCluster.CreditCard -> if (autofillItem is AutofillItem.CreditCard) {
                return mapCreditCardFields(encryptionContext, autofillItem, cluster)
            }

            is NodeCluster.Login -> if (autofillItem is AutofillItem.Login) {
                return mapLoginFields(encryptionContext, autofillItem, cluster)
            }

            is NodeCluster.SignUp -> if (autofillItem is AutofillItem.Login) {
                return mapSignUpFields(encryptionContext, autofillItem, cluster)
            }
        }

        PassLogger.e(
            TAG,
            "Could not find any combination of mappings for item: ${autofillItem.type()} and cluster ${cluster.type()}"
        )
        return AutofillMappings(emptyList())
    }

}

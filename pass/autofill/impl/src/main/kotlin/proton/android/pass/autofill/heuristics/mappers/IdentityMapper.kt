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

import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.log.api.PassLogger

object IdentityMapper {

    private const val TAG = "IdentityMapper"

    fun mapIdentityFields(autofillItem: AutofillItem.Identity, cluster: NodeCluster.Identity): AutofillMappings {
        val mappings = mutableListOf<DatasetMapping>()

        mappings.add(
            DatasetMapping(
                autofillFieldId = cluster.fullName.id,
                contents = autofillItem.fullName,
                displayValue = autofillItem.fullName
            )
        )
        if (cluster.address != null && autofillItem.address != null) {
            mappings.add(
                DatasetMapping(
                    autofillFieldId = cluster.address.id,
                    contents = autofillItem.address,
                    displayValue = autofillItem.address
                )
            )
        }
        if (cluster.postalCode != null && autofillItem.postalCode != null) {
            mappings.add(
                DatasetMapping(
                    autofillFieldId = cluster.postalCode.id,
                    contents = autofillItem.postalCode,
                    displayValue = autofillItem.postalCode
                )
            )
        }
        if (cluster.phoneNumber != null && autofillItem.phoneNumber != null) {
            mappings.add(
                DatasetMapping(
                    autofillFieldId = cluster.phoneNumber.id,
                    contents = autofillItem.phoneNumber,
                    displayValue = autofillItem.phoneNumber
                )
            )
        }

        if (mappings.isEmpty()) {
            PassLogger.w(TAG, "No mappings found for autofill")
        }

        return AutofillMappings(mappings)
    }
}

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

import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.log.api.PassLogger

object IdentityMapper {

    private const val TAG = "IdentityMapper"

    fun mapIdentityFields(autofillItem: AutofillItem.Identity, cluster: NodeCluster.Identity): AutofillMappings {
        val mappings = mutableListOf<DatasetMapping>()
        mappings.addMapping(cluster.fullName, autofillItem.fullName)
        mappings.addMapping(cluster.address, autofillItem.address)
        mappings.addMapping(cluster.city, autofillItem.city)
        mappings.addMapping(cluster.postalCode, autofillItem.postalCode)
        mappings.addMapping(cluster.phoneNumber, autofillItem.phoneNumber)

        if (mappings.isEmpty()) {
            PassLogger.w(TAG, "No mappings found for autofill")
        }

        return AutofillMappings(mappings)
    }

    private fun MutableList<DatasetMapping>.addMapping(clusterField: AssistField?, autofillField: String?) {
        if (clusterField != null && autofillField != null) {
            add(
                DatasetMapping(
                    autofillFieldId = clusterField.id,
                    contents = autofillField,
                    displayValue = autofillField
                )
            )
        }
    }
}

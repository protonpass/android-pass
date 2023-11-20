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

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.log.api.PassLogger

object ItemFieldMapper {

    private const val TAG = "ItemFieldMapper"

    fun mapFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem,
        cluster: NodeCluster
    ): AutofillMappings = when (autofillItem) {
        is AutofillItem.Login -> {
            mapLoginFields(
                encryptionContext = encryptionContext,
                autofillItem = autofillItem,
                cluster = cluster
            )
        }
    }

    @Suppress("LongParameterList")
    private fun mapLoginFields(
        encryptionContext: EncryptionContext,
        autofillItem: AutofillItem.Login,
        cluster: NodeCluster
    ): AutofillMappings {
        val mappings = mutableListOf<DatasetMapping>()
        when (cluster) {
            NodeCluster.Empty -> {
                PassLogger.e(TAG, "Expected NodeCluster to be non-empty")
            }

            is NodeCluster.SignUp -> {
                mappings.add(mappingForUsername(autofillItem.username, cluster.username.id))
                mappings.add(
                    mappingForPassword(
                        encryptionContext,
                        autofillItem.password,
                        cluster.password.id
                    )
                )
                mappings.add(
                    mappingForPassword(
                        encryptionContext,
                        autofillItem.password,
                        cluster.repeatPassword.id
                    )
                )
            }

            is NodeCluster.Login -> when (cluster) {
                is NodeCluster.Login.OnlyPassword -> {
                    mappings.add(
                        mappingForPassword(
                            encryptionContext,
                            autofillItem.password,
                            cluster.password.id
                        )
                    )
                }

                is NodeCluster.Login.OnlyUsername -> {
                    mappings.add(mappingForUsername(autofillItem.username, cluster.username.id))
                }

                is NodeCluster.Login.UsernameAndPassword -> {
                    mappings.add(mappingForUsername(autofillItem.username, cluster.username.id))
                    mappings.add(
                        mappingForPassword(
                            encryptionContext,
                            autofillItem.password,
                            cluster.password.id
                        )
                    )
                }
            }
        }

        if (mappings.isEmpty()) {
            val message = "No mappings found for autofill"
            PassLogger.e(TAG, IllegalStateException(message), message)
        }

        return AutofillMappings(mappings)
    }

    private fun mappingForPassword(
        encryptionContext: EncryptionContext,
        password: EncryptedString?,
        id: AutofillFieldId
    ) = DatasetMapping(
        autofillFieldId = id,
        contents = password?.let { encryptionContext.decrypt(it) } ?: "",
        displayValue = ""
    )

    private fun mappingForUsername(username: String, id: AutofillFieldId) = DatasetMapping(
        autofillFieldId = id,
        contents = username,
        displayValue = username
    )
}

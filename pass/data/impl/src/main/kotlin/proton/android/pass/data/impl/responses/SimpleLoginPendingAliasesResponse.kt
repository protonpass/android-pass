/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimpleLoginPendingAliasesResponse(
    @SerialName("PendingAliases")
    val pendingAliases: SimpleLoginPendingAliasesData
)

@Serializable
data class SimpleLoginPendingAliasesData(
    @SerialName("Aliases")
    val aliases: List<SimpleLoginAliasData>,
    @SerialName("Total")
    val total: Int,
    @SerialName("LastToken")
    val lastToken: String?
)

@Serializable
data class SimpleLoginAliasData(
    @SerialName("PendingAliasID")
    val pendingAliasID: String,
    @SerialName("AliasEmail")
    val aliasEmail: String
)

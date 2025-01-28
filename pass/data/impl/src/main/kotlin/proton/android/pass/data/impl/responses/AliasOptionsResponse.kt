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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAliasOptionsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Options")
    val options: AliasOptionsResponse
)

@Serializable
data class AliasOptionsResponse(
    @SerialName("Suffixes")
    val suffixes: List<AliasSuffixResponse>,
    @SerialName("Mailboxes")
    val mailboxes: List<AliasMailboxResponse>
)

@Serializable
data class AliasSuffixResponse(
    @SerialName("Suffix")
    val suffix: String,
    @SerialName("SignedSuffix")
    val signedSuffix: String,
    @SerialName("IsCustom")
    val isCustom: Boolean,
    @SerialName("IsPremium")
    val isPremium: Boolean,
    @SerialName("Domain")
    val domain: String
)

@Serializable
data class AliasMailboxResponse(
    @SerialName("ID")
    val id: Int,
    @SerialName("Email")
    val email: String
)

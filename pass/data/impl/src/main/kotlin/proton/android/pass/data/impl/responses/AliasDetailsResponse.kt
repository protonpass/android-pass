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
data class AliasDetailsResponse(
    @SerialName("Alias")
    val alias: AliasResponse
)

@Serializable
data class AliasResponse(
    @SerialName("Email")
    val email: String,
    @SerialName("Modify")
    val modify: Boolean,
    @SerialName("Mailboxes")
    val mailboxes: List<AliasMailboxResponse>,
    @SerialName("AvailableMailboxes")
    val availableMailboxes: List<AliasMailboxResponse>,
    @SerialName("Stats")
    val stats: AliasStatsResponse,
    @SerialName("Name")
    val name: String?,
    @SerialName("DisplayName")
    val displayName: String,
    @SerialName("Note")
    val note: String?
)

@Serializable
data class AliasStatsResponse(
    @SerialName("ForwardedEmails")
    val forwardedEmails: Int,
    @SerialName("RepliedEmails")
    val repliedEmails: Int,
    @SerialName("BlockedEmails")
    val blockedEmails: Int
)

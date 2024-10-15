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

package proton.android.pass.data.impl.responses.aliascontacts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactResponse(
    @SerialName("ID")
    val id: Int,
    @SerialName("Name")
    val name: String?,
    @SerialName("Blocked")
    val blocked: Boolean,
    @SerialName("ReverseAlias")
    val reverseAlias: String,
    @SerialName("Email")
    val email: String,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("RepliedEmails")
    val repliedEmails: Int = 0,
    @SerialName("ForwardedEmails")
    val forwardedEmails: Int = 0,
    @SerialName("BlockedEmails")
    val blockedEmails: Int = 0
)

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
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId

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
    val repliedEmails: Int? = null,
    @SerialName("ForwardedEmails")
    val forwardedEmails: Int? = null,
    @SerialName("BlockedEmails")
    val blockedEmails: Int? = null
)

fun ContactResponse.toDomain(): Contact = Contact(
    id = ContactId(this.id),
    name = this.name,
    blocked = this.blocked,
    reverseAlias = this.reverseAlias,
    email = this.email,
    createTime = this.createTime,
    repliedEmails = this.repliedEmails,
    forwardedEmails = this.forwardedEmails,
    blockedEmails = this.blockedEmails
)

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
import proton.android.pass.domain.aliascontacts.AliasContacts

@Serializable
data class GetAliasContactsResponse(
    @SerialName("Contacts")
    val contacts: List<ContactResponse>,
    @SerialName("Total")
    val total: Int,
    @SerialName("LastID")
    val lastId: Int,
    @SerialName("Code")
    val code: Int
)

fun GetAliasContactsResponse.toDomain(): AliasContacts = AliasContacts(
    contacts = this.contacts.map(ContactResponse::toDomain),
    total = this.total
)

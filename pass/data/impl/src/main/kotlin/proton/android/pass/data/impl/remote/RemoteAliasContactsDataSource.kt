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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.requests.aliascontacts.CreateAliasContactRequest
import proton.android.pass.data.impl.requests.aliascontacts.UpdateBlockedAliasContactRequest
import proton.android.pass.data.impl.responses.aliascontacts.CreateAliasContactResponse
import proton.android.pass.data.impl.responses.aliascontacts.GetAliasContactResponse
import proton.android.pass.data.impl.responses.aliascontacts.GetAliasContactsResponse
import proton.android.pass.data.impl.responses.aliascontacts.UpdateBlockedAliasContactResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.ContactId

interface RemoteAliasContactsDataSource {

    suspend fun getAliasContacts(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        since: ContactId?
    ): GetAliasContactsResponse

    suspend fun getAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): GetAliasContactResponse

    suspend fun createAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        request: CreateAliasContactRequest
    ): CreateAliasContactResponse

    suspend fun deleteAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    )

    suspend fun updateBlockedAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId,
        request: UpdateBlockedAliasContactRequest
    ): UpdateBlockedAliasContactResponse
}

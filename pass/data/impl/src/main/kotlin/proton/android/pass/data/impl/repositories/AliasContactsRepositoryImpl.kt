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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.repositories.AliasContactsRepository
import proton.android.pass.data.impl.remote.RemoteAliasContactsDataSource
import proton.android.pass.data.impl.requests.aliascontacts.CreateAliasContactRequest
import proton.android.pass.data.impl.requests.aliascontacts.UpdateBlockedAliasContactRequest
import proton.android.pass.data.impl.responses.aliascontacts.ContactResponse
import proton.android.pass.data.impl.responses.aliascontacts.toDomain
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.AliasContacts
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId
import javax.inject.Inject

class AliasContactsRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasContactsDataSource
) : AliasContactsRepository {

    override suspend fun observeAliasContacts(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        fullList: Boolean
    ): Flow<AliasContacts> = flow {
        val allContacts = mutableListOf<Contact>()
        var lastId: ContactId? = null
        var total: Int

        do {
            val response = remoteDataSource.getAliasContacts(userId, shareId, itemId, lastId)
            total = response.total
            allContacts.addAll(response.contacts.map(ContactResponse::toDomain))
            lastId = if (response.contacts.isNotEmpty()) ContactId(response.lastId) else null
        } while (fullList && lastId != null)

        emit(AliasContacts(allContacts, total))
    }

    override suspend fun observeAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): Flow<Contact> = oneShot {
        remoteDataSource.getAliasContact(userId, shareId, itemId, contactId)
            .contact
            .toDomain()
    }

    override suspend fun createAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        email: String,
        name: String?
    ): Contact = remoteDataSource.createAliasContact(
        userId,
        shareId,
        itemId,
        CreateAliasContactRequest(email, name)
    ).contact.toDomain()

    override suspend fun deleteAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ) = remoteDataSource.deleteAliasContact(userId, shareId, itemId, contactId)

    override suspend fun updateBlockedAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId,
        blocked: Boolean
    ): Contact = remoteDataSource.updateBlockedAliasContact(
        userId,
        shareId,
        itemId,
        contactId,
        UpdateBlockedAliasContactRequest(blocked)
    ).contact.toDomain()
}

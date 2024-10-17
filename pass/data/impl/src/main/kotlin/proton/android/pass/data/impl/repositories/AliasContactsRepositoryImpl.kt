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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
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

    private data class CacheKey(
        val userId: UserId,
        val shareId: ShareId,
        val itemId: ItemId,
        val contactId: ContactId?
    )

    private val contactsCache = MutableStateFlow<Map<CacheKey, Contact>>(emptyMap())

    override suspend fun observeAliasContacts(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        fullList: Boolean
    ): Flow<AliasContacts> = contactsCache.map { cachedContacts ->
        val filteredContacts = cachedContacts.filterKeys {
            it.userId == userId && it.shareId == shareId && it.itemId == itemId
        }.values.toList()

        AliasContacts(filteredContacts, filteredContacts.size)
    }.onStart {
        val allContacts = mutableListOf<Contact>()
        var lastId: ContactId? = null
        var total: Int

        do {
            val response = remoteDataSource.getAliasContacts(userId, shareId, itemId, lastId)
            total = response.total
            val newContacts = response.contacts.map(ContactResponse::toDomain)

            val updatedCache = contactsCache.value.toMutableMap()
            newContacts.forEach { contact ->
                val cacheKey = CacheKey(userId, shareId, itemId, contact.id)
                updatedCache[cacheKey] = contact
            }
            contactsCache.value = updatedCache

            allContacts.addAll(newContacts)
            lastId = if (response.contacts.isNotEmpty()) ContactId(response.lastId) else null
        } while (fullList && lastId != null)

        emit(AliasContacts(allContacts, total))
    }

    override suspend fun observeAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): Flow<Contact> = contactsCache.mapNotNull { cachedContacts ->
        val cacheKey = CacheKey(userId, shareId, itemId, contactId)
        cachedContacts[cacheKey]
    }.onStart {
        val refreshedContact = remoteDataSource.getAliasContact(userId, shareId, itemId, contactId)
            .contact
            .toDomain()

        val updatedCache = contactsCache.value.toMutableMap()
        updatedCache[CacheKey(userId, shareId, itemId, contactId)] = refreshedContact
        contactsCache.value = updatedCache

        emit(refreshedContact)
    }

    override suspend fun createAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        email: String,
        name: String?
    ): Contact {
        val contact = remoteDataSource.createAliasContact(
            userId, shareId, itemId, CreateAliasContactRequest(email, name)
        ).contact.toDomain()

        val cacheKey = CacheKey(userId, shareId, itemId, contact.id)
        val updatedCache = contactsCache.value.toMutableMap()
        updatedCache[cacheKey] = contact
        contactsCache.value = updatedCache

        return contact
    }

    override suspend fun deleteAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ) {
        remoteDataSource.deleteAliasContact(userId, shareId, itemId, contactId)

        val cacheKey = CacheKey(userId, shareId, itemId, contactId)
        val updatedCache = contactsCache.value.toMutableMap()
        updatedCache.remove(cacheKey)
        contactsCache.value = updatedCache
    }

    override suspend fun updateBlockedAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId,
        blocked: Boolean
    ): Contact {
        val updatedContact = remoteDataSource.updateBlockedAliasContact(
            userId, shareId, itemId, contactId, UpdateBlockedAliasContactRequest(blocked)
        ).contact.toDomain()

        val cacheKey = CacheKey(userId, shareId, itemId, contactId)
        val updatedCache = contactsCache.value.toMutableMap()
        updatedCache[cacheKey] = updatedContact
        contactsCache.value = updatedCache

        return updatedContact
    }
}

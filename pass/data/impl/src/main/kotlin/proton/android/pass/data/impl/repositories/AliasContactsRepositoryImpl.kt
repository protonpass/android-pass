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
import proton.android.pass.common.api.safeRunCatching
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
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

data class ContactCacheKey(
    val shareId: ShareId,
    val itemId: ItemId,
    val contactId: ContactId? = null
)

class AliasContactsRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasContactsDataSource
) : AliasContactsRepository {

    private val contactsCache = MutableStateFlow<Map<ContactCacheKey, Contact>>(emptyMap())

    override suspend fun observeAliasContacts(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        fullList: Boolean
    ): Flow<AliasContacts> = contactsCache.map { cachedContacts ->
        val relevantContacts = cachedContacts.filterKeys {
            it.shareId == shareId && it.itemId == itemId && it.contactId != null
        }.values.toList()

        AliasContacts(relevantContacts, relevantContacts.size)
    }.onStart {
        val cachedContacts = contactsCache.value.filterKeys {
            it.shareId == shareId && it.itemId == itemId && it.contactId != null
        }.values.toList()

        emit(AliasContacts(cachedContacts, cachedContacts.size))

        val allContacts = mutableListOf<Contact>()
        var lastId: ContactId? = null
        var total: Int

        safeRunCatching {
            do {
                val response = remoteDataSource.getAliasContacts(userId, shareId, itemId, lastId)
                total = response.total
                val newContacts = response.contacts.map(ContactResponse::toDomain)

                val updatedContacts = contactsCache.value.toMutableMap()
                newContacts.forEach { contact ->
                    val cacheKey = ContactCacheKey(shareId, itemId, contact.id)
                    updatedContacts[cacheKey] = contact
                }

                contactsCache.value = updatedContacts

                allContacts.addAll(newContacts)
                lastId = if (response.contacts.isNotEmpty()) ContactId(response.lastId) else null
            } while (fullList && lastId != null)

            emit(AliasContacts(allContacts, total))
        }.onFailure {
            PassLogger.w(TAG, "Failed to fetch alias contacts")
            PassLogger.w(TAG, it)
        }
    }

    override suspend fun observeAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): Flow<Contact> = contactsCache.mapNotNull { cachedContacts ->
        cachedContacts[ContactCacheKey(shareId, itemId, contactId)]
    }.onStart {
        val cacheKey = ContactCacheKey(shareId, itemId, contactId)
        val cachedContact = contactsCache.value[cacheKey]
        if (cachedContact != null) {
            emit(cachedContact)
        }

        safeRunCatching {
            val refreshedContact = remoteDataSource.getAliasContact(userId, shareId, itemId, contactId)
                .contact
                .toDomain()

            contactsCache.value = contactsCache.value.toMutableMap().apply {
                this[cacheKey] = refreshedContact
            }

            emit(refreshedContact)
        }.onFailure {
            PassLogger.w(TAG, "Failed to fetch alias contact")
            PassLogger.w(TAG, it)
        }
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

        val cacheKey = ContactCacheKey(shareId, itemId, contact.id)

        contactsCache.value = contactsCache.value.toMutableMap().apply {
            this[cacheKey] = contact
        }

        return contact
    }

    override suspend fun deleteAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ) {
        remoteDataSource.deleteAliasContact(userId, shareId, itemId, contactId)

        val cacheKey = ContactCacheKey(shareId, itemId, contactId)
        val updatedContacts = contactsCache.value.toMutableMap()
        updatedContacts.remove(cacheKey)

        contactsCache.value = updatedContacts
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

        val cacheKey = ContactCacheKey(shareId, itemId, contactId)

        contactsCache.value = contactsCache.value.toMutableMap().apply {
            this[cacheKey] = updatedContact
        }

        return updatedContact
    }

    companion object {
        private const val TAG = "AliasContactsRepositoryImpl"
    }
}

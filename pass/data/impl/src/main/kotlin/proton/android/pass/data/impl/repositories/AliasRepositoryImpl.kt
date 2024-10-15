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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.firstError
import proton.android.pass.data.api.repositories.AliasItemsChangeStatusResult
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.remote.RemoteAliasDataSource
import proton.android.pass.data.impl.requests.ChangeAliasStatusRequest
import proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import proton.android.pass.data.impl.requests.aliascontacts.CreateAliasContactRequest
import proton.android.pass.data.impl.requests.aliascontacts.GetAliasContactsRequest
import proton.android.pass.data.impl.requests.aliascontacts.UpdateBlockedAliasContactRequest
import proton.android.pass.data.impl.responses.AliasMailboxResponse
import proton.android.pass.data.impl.responses.aliascontacts.ContactResponse
import proton.android.pass.data.impl.responses.aliascontacts.toDomain
import proton.android.pass.domain.AliasDetails
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasOptions
import proton.android.pass.domain.AliasStats
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId
import javax.inject.Inject

class AliasRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasDataSource
) : AliasRepository {

    override fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions> =
        remoteDataSource.getAliasOptions(userId, shareId)
            .map { it.toDomain() }
            .flowOn(Dispatchers.IO)

    override fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails> = remoteDataSource.getAliasDetails(userId, shareId, itemId)
        .map { details ->
            AliasDetails(
                email = details.email,
                mailboxes = mapMailboxes(details.mailboxes),
                availableMailboxes = mapMailboxes(details.availableMailboxes),
                stats = AliasStats(
                    forwardedEmails = details.stats.forwardedEmails,
                    repliedEmails = details.stats.repliedEmails,
                    blockedEmails = details.stats.blockedEmails
                )
            )
        }
        .flowOn(Dispatchers.IO)

    override fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit> {
        val request = UpdateAliasMailboxesRequest(
            mailboxIds = mailboxes.map { it.id }
        )
        return remoteDataSource.updateAliasMailboxes(userId, shareId, itemId, request)
            .map { }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun changeAliasStatus(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        enable: Boolean
    ) {
        val request = ChangeAliasStatusRequest(enable)
        remoteDataSource.changeAliasStatus(userId, shareId, itemId, request)
    }

    override suspend fun changeAliasStatus(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>,
        enabled: Boolean
    ): AliasItemsChangeStatusResult = coroutineScope {
        val results: List<Result<Pair<ShareId, ItemId>>> = items.map { (shareId, itemId) ->
            async {
                runCatching {
                    changeAliasStatus(userId, shareId, itemId, enabled)
                        .let { shareId to itemId }
                }
            }
        }.awaitAll().toList()
        val (successes, failures) = results.partition { it.isSuccess }
        when {
            failures.isEmpty() && successes.isNotEmpty() -> AliasItemsChangeStatusResult.AllChanged(
                items = successes.map { it.getOrThrow() }
            )
            successes.isEmpty() -> AliasItemsChangeStatusResult.NoneChanged(
                exception = failures.firstError() ?: IllegalStateException("No results")
            )
            else -> AliasItemsChangeStatusResult.SomeChanged(
                items = successes.map { it.getOrThrow() }
            )
        }
    }

    override suspend fun observeAliasContacts(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Contact>> = oneShot {
        remoteDataSource.getAliasContacts(userId, shareId, itemId, GetAliasContactsRequest())
            .contacts
            .map(ContactResponse::toDomain)
    }.flowOn(Dispatchers.IO)

    override suspend fun observeAliasContact(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): Flow<Contact> = oneShot {
        remoteDataSource.getAliasContact(userId, shareId, itemId, contactId)
            .contact
            .toDomain()
    }.flowOn(Dispatchers.IO)

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

    private fun mapMailboxes(input: List<AliasMailboxResponse>): List<AliasMailbox> =
        input.map { AliasMailbox(id = it.id, email = it.email) }
}

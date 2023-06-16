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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.remote.RemoteAliasDataSource
import proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import proton.android.pass.data.impl.responses.AliasMailboxResponse
import me.proton.core.domain.entity.UserId
import proton.pass.domain.AliasDetails
import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasOptions
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
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
    ): Flow<AliasDetails> =
        remoteDataSource.getAliasDetails(userId, shareId, itemId)
            .map { details ->
                AliasDetails(
                    email = details.email,
                    mailboxes = mapMailboxes(details.mailboxes),
                    availableMailboxes = mapMailboxes(details.availableMailboxes)
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

    private fun mapMailboxes(input: List<AliasMailboxResponse>): List<AliasMailbox> =
        input.map { AliasMailbox(id = it.id, email = it.email) }
}

package me.proton.pass.data.repositories

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.data.extensions.toDomain
import me.proton.pass.data.remote.RemoteAliasDataSource
import me.proton.pass.data.requests.UpdateAliasMailboxesRequest
import me.proton.pass.data.responses.AliasMailboxResponse
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.AliasRepository
import javax.inject.Inject

class AliasRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasDataSource
) : AliasRepository {

    override suspend fun getAliasOptions(userId: UserId, shareId: ShareId): Result<AliasOptions> {
        val response = remoteDataSource.getAliasOptions(userId, shareId)
        return response.map { it.toDomain() }
    }

    override suspend fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<AliasDetails> {
        val response = remoteDataSource.getAliasDetails(userId, shareId, itemId)
        return response.map { details ->
            AliasDetails(
                email = details.email,
                mailboxes = mapMailboxes(details.mailboxes),
                availableMailboxes = mapMailboxes(details.availableMailboxes)
            )
        }
    }

    override suspend fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Result<Unit> {
        val request = UpdateAliasMailboxesRequest(
            mailboxIds = mailboxes.map { it.id }
        )
        return remoteDataSource.updateAliasMailboxes(userId, shareId, itemId, request).map { }
    }

    private fun mapMailboxes(input: List<AliasMailboxResponse>): List<AliasMailbox> =
        input.map { AliasMailbox(id = it.id, email = it.email) }
}

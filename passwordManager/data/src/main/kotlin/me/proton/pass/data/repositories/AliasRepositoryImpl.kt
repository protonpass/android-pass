package me.proton.pass.data.repositories

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.data.extensions.toDomain
import me.proton.pass.data.remote.RemoteAliasDataSource
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

    override suspend fun getAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<List<AliasMailbox>> {
        val response = remoteDataSource.getAliasDetails(userId, shareId, itemId)
        return response.map { it.mailboxes.map { AliasMailbox(id = it.id, email = it.email) } }
    }
}

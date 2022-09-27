package me.proton.core.pass.data.repositories

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.extensions.toDomain
import me.proton.core.pass.data.remote.RemoteAliasDataSource
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.AliasRepository
import javax.inject.Inject

class AliasRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasDataSource
) : AliasRepository {

    override suspend fun getAliasOptions(userId: UserId, shareId: ShareId): AliasOptions {
        val response = remoteDataSource.getAliasOptions(userId, shareId)
        return response.toDomain()
    }

    override suspend fun getAliasMailboxes(userId: UserId, shareId: ShareId, itemId: ItemId): List<AliasMailbox> {
        val response = remoteDataSource.getAliasDetails(userId, shareId, itemId)
        return response.mailboxes.map { AliasMailbox(id = it.id, email = it.email) }
    }
}

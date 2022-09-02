package me.proton.core.pass.data.repositories

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.extensions.toDomain
import me.proton.core.pass.data.remote.RemoteAliasDataSource
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.AliasRepository

class AliasRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAliasDataSource
) : AliasRepository {

    override suspend fun getAliasOptions(userId: UserId, shareId: ShareId): AliasOptions {
        val response = remoteDataSource.getAliasOptions(userId, shareId)
        return response.toDomain()
    }

    override suspend fun getAliasMailboxes(userId: UserId, shareId: ShareId, itemId: ItemId): List<String> {
        val response = remoteDataSource.getAliasDetails(userId, shareId, itemId)
        return response.mailboxes
    }
}

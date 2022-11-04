package me.proton.pass.data.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.data.db.entities.ShareEntity
import me.proton.pass.domain.ShareId
import me.proton.core.user.domain.entity.AddressId

interface LocalShareDataSource {
    suspend fun upsertShares(shares: List<ShareEntity>)
    suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity?
    fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>>
    fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>>
    suspend fun deleteShare(shareId: ShareId): Boolean
    suspend fun hasShares(userId: UserId): Boolean
}

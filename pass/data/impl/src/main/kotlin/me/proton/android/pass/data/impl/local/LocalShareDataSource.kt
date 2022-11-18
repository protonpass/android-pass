package me.proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.pass.domain.ShareId

interface LocalShareDataSource {
    suspend fun upsertShares(shares: List<ShareEntity>)
    suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity?
    fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>>
    fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>>
    suspend fun deleteShare(shareId: ShareId): Boolean
    suspend fun hasShares(userId: UserId): Boolean
}

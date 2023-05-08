package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.pass.domain.ShareId

interface LocalShareDataSource {
    suspend fun upsertShares(shares: List<ShareEntity>)
    suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity?
    fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>>
    fun observeAllActiveSharesForUser(userId: UserId): Flow<List<ShareEntity>>
    fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>>
    fun observeActiveVaultCount(userId: UserId): Flow<Int>
    suspend fun deleteShares(shareIds: Set<ShareId>): Boolean
    suspend fun hasShares(userId: UserId): Boolean
    suspend fun disablePrimaryShare(userId: UserId)
    suspend fun setPrimaryShareStatus(userId: UserId, shareId: ShareId, isPrimary: Boolean)
    suspend fun deleteSharesForUser(userId: UserId)
}

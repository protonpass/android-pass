package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.pass.domain.ShareId
import javax.inject.Inject

class LocalShareDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalShareDataSource {

    override suspend fun upsertShares(shares: List<ShareEntity>) =
        database.sharesDao().insertOrUpdate(*shares.toTypedArray())

    override suspend fun evictAndUpsertShares(userId: UserId, shares: List<ShareEntity>) =
        database.sharesDao().evictAndUpsertShares(userId, *shares.toTypedArray())

    override suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity? =
        database.sharesDao().getById(userId.id, shareId.id)

    override fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllForUser(userId.id)

    override fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllForAddress(addressId.id)

    override suspend fun deleteShare(shareId: ShareId): Boolean =
        database.sharesDao().delete(shareId.id) > 0

    override suspend fun hasShares(userId: UserId): Boolean =
        database.sharesDao().countShares(userId.id) > 0
}

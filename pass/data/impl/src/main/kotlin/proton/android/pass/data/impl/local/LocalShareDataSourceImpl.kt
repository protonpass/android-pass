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

    override suspend fun getById(userId: UserId, shareId: ShareId): ShareEntity? =
        database.sharesDao().getById(userId.id, shareId.id)

    override fun getAllSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllForUser(userId.id)

    override fun observeAllActiveSharesForUser(userId: UserId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllActiveForUser(userId.id)

    override fun getAllSharesForAddress(addressId: AddressId): Flow<List<ShareEntity>> =
        database.sharesDao().observeAllForAddress(addressId.id)

    override suspend fun deleteShares(shareIds: Set<ShareId>): Boolean =
        database.sharesDao().delete(shareIds.map { it.id }.toTypedArray()) > 0

    override suspend fun hasShares(userId: UserId): Boolean =
        database.sharesDao().countShares(userId.id) > 0

    override suspend fun disablePrimaryShare(userId: UserId) =
        database.sharesDao().disablePrimaryShares(userId.id)

    override suspend fun setPrimaryShareStatus(
        userId: UserId,
        shareId: ShareId,
        isPrimary: Boolean
    ) = database.sharesDao().setPrimaryShareStatus(userId.id, shareId.id, isPrimary)

    override suspend fun deleteSharesForUser(userId: UserId) {
        database.sharesDao().deleteSharesForUser(userId.id)
    }

    override fun observeActiveVaultCount(userId: UserId): Flow<Int> =
        database.sharesDao().observeActiveVaultCount(userId.id)
}

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.pass.domain.ShareId
import javax.inject.Inject

class LocalShareKeyDataSourceImpl @Inject constructor(
    private val passDatabase: PassDatabase
) : LocalShareKeyDataSource {
    override fun getAllShareKeysForShare(
        userId: UserId,
        shareId: ShareId
    ): Flow<List<ShareKeyEntity>> =
        passDatabase.shareKeysDao().getAllForShare(userId.id, shareId.id)

    override fun getForShareAndRotation(
        userId: UserId,
        shareId: ShareId,
        rotation: Long
    ): Flow<ShareKeyEntity?> =
        passDatabase.shareKeysDao().getByShareAndRotation(userId.id, shareId.id, rotation)

    override suspend fun storeShareKeys(entities: List<ShareKeyEntity>) {
        passDatabase.shareKeysDao().insertOrUpdate(*entities.toTypedArray())
    }
}

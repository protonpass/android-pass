package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.pass.domain.ShareId

interface LocalShareKeyDataSource {
    fun getAllShareKeysForShare(userId: UserId, shareId: ShareId): Flow<List<ShareKeyEntity>>
    fun getForShareAndRotation(
        userId: UserId,
        shareId: ShareId,
        rotation: Long
    ): Flow<ShareKeyEntity?>
    suspend fun storeShareKeys(entities: List<ShareKeyEntity>)
}

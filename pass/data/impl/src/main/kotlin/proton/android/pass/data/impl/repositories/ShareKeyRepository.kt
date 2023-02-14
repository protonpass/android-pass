package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.pass.domain.ShareId

data class ShareKey(
    val rotation: Long,
    val key: EncryptedByteArray,
    val responseKey: String,
    val createTime: Long
)

interface ShareKeyRepository {
    fun getShareKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        forceRefresh: Boolean = false,
        shouldStoreLocally: Boolean = true
    ): Flow<List<ShareKey>>
    suspend fun saveShareKeys(shareKeyEntities: List<ShareKeyEntity>)
}


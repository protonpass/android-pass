package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.pass.domain.ShareId
import proton.pass.domain.key.ShareKey

interface ShareKeyRepository {
    fun getShareKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        forceRefresh: Boolean = false,
        shouldStoreLocally: Boolean = true
    ): Flow<List<ShareKey>>

    fun getLatestKeyForShare(shareId: ShareId): Flow<ShareKey>
    fun getShareKeyForRotation(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        keyRotation: Long
    ): Flow<ShareKey?>

    suspend fun saveShareKeys(shareKeyEntities: List<ShareKeyEntity>)
}


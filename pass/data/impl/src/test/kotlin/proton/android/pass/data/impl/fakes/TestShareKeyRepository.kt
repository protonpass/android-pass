package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.pass.domain.ShareId
import proton.pass.domain.key.ShareKey

class TestShareKeyRepository : ShareKeyRepository {

    private var getShareKeysFlow = testFlow<List<ShareKey>>()
    private var getLatestKeyForShareFlow = testFlow<ShareKey>()
    private var getShareKeyForRotationFlow = testFlow<ShareKey?>()

    fun emitGetShareKeys(value: List<ShareKey>) {
        getShareKeysFlow.tryEmit(value)
    }

    fun emitGetLatestKeyForShare(value: ShareKey) {
        getLatestKeyForShareFlow.tryEmit(value)
    }

    fun emitGetShareKeyForRotation(value: ShareKey?) {
        getShareKeyForRotationFlow.tryEmit(value)
    }

    override fun getShareKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean
    ): Flow<List<ShareKey>> = getShareKeysFlow

    override fun getLatestKeyForShare(shareId: ShareId): Flow<ShareKey> = getLatestKeyForShareFlow

    override fun getShareKeyForRotation(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        keyRotation: Long
    ): Flow<ShareKey?> = getShareKeyForRotationFlow

    override suspend fun saveShareKeys(shareKeyEntities: List<ShareKeyEntity>) {

    }
}

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSource
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.ShareKey
import javax.inject.Inject

class ItemKeyRepositoryImpl @Inject constructor(
    private val shareKeyRepository: ShareKeyRepository,
    private val remoteItemKeyRepository: RemoteItemKeyDataSource,
    private val openItemKey: OpenItemKey
) : ItemKeyRepository {

    override fun getLatestItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Pair<ShareKey, ItemKey>> = flow {
        val response = remoteItemKeyRepository.fetchLatestItemKey(userId, shareId, itemId)
        val shareKey = shareKeyRepository
            .getShareKeyForRotation(userId, addressId, shareId, response.keyRotation)
            .first()

        if (shareKey == null) {
            throw KeyNotFound("Could not find ShareKey [shareId=${shareId.id}] [keyRotation=${response.keyRotation}]")
        }

        val itemKey = openItemKey(shareKey, response.toCrypto())
        emit(shareKey to itemKey)
    }
}

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.crypto.ReencryptShareKey
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.pass.domain.ShareId
import javax.inject.Inject

class ShareKeyRepositoryImpl @Inject constructor(
    private val reencryptShareKey: ReencryptShareKey,
    private val userRepository: UserRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val localDataSource: LocalShareKeyDataSource,
    private val remoteDataSource: RemoteShareKeyDataSource
) : ShareKeyRepository {

    override fun getShareKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean
    ): Flow<List<ShareKey>> = flow {
        if (!forceRefresh) {
            val localKeys = localDataSource.getAllShareKeysForShare(userId, shareId).first()
            if (localKeys.isNotEmpty()) {
                emit(localKeys.map(::entityToDomain))
                return@flow
            }
        }

        val remoteKeys = remoteDataSource.getShareKeys(userId, shareId).first()
        val user = userRepository.getUser(userId)

        val mapped = encryptionContextProvider.withEncryptionContext {
            remoteKeys.map { response ->
                val reencryptedKey = reencryptShareKey(this@withEncryptionContext, user, response.key)
                ShareKeyEntity(
                    rotation = response.keyRotation,
                    userId = userId.id,
                    addressId = addressId.id,
                    shareId = shareId.id,
                    key = response.key,
                    createTime = response.createTime,
                    symmetricallyEncryptedKey = reencryptedKey
                )
            }
        }

        if (shouldStoreLocally) {
            localDataSource.storeShareKeys(mapped)
        }
        emit(mapped.map(::entityToDomain))
    }

    override suspend fun saveShareKeys(shareKeyEntities: List<ShareKeyEntity>) {
        localDataSource.storeShareKeys(shareKeyEntities)
    }

    private fun entityToDomain(entity: ShareKeyEntity): ShareKey =
        ShareKey(
            rotation = entity.rotation,
            key = entity.symmetricallyEncryptedKey,
            responseKey = entity.key,
            createTime = entity.createTime
        )
}

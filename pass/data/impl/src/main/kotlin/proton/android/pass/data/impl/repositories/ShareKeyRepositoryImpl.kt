package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.crypto.ReencryptShareKey
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.pass.domain.ShareId
import proton.pass.domain.key.ShareKey
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

        val keys = requestRemoteKeys(userId, addressId, shareId)
        if (shouldStoreLocally) {
            localDataSource.storeShareKeys(keys)
        }

        emit(keys.map(::entityToDomain))
    }

    override suspend fun saveShareKeys(shareKeyEntities: List<ShareKeyEntity>) {
        localDataSource.storeShareKeys(shareKeyEntities)
    }

    override fun getLatestKeyForShare(shareId: ShareId): Flow<ShareKey> =
        localDataSource.getLatestKeyForShare(shareId).map(::entityToDomain)

    override fun getShareKeyForRotation(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        keyRotation: Long
    ): Flow<ShareKey?> = flow {
        val localKeys = localDataSource.getAllShareKeysForShare(userId, shareId).first()
        val key = localKeys.firstOrNull { it.rotation == keyRotation }
        if (key != null) {
            val mapped = entityToDomain(key)
            emit(mapped)
            return@flow
        }

        // Key was not present, force a refresh
        val retrievedKeys = requestRemoteKeys(userId, addressId, shareId)
        localDataSource.storeShareKeys(retrievedKeys)

        val retrievedKey = retrievedKeys.firstOrNull { it.rotation == keyRotation }
        if (retrievedKey != null) {
            val mapped = entityToDomain(retrievedKey)
            emit(mapped)
        } else {
            emit(null)
        }
    }

    private suspend fun requestRemoteKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId
    ): List<ShareKeyEntity> {
        val remoteKeys = remoteDataSource.getShareKeys(userId, shareId).first()
        val user = userRepository.getUser(userId)

        return encryptionContextProvider.withEncryptionContext {
            remoteKeys.map { response ->
                val keyData = runCatching {
                    reencryptShareKey(
                        encryptionContext = this@withEncryptionContext,
                        user = user,
                        keyResponse = response
                    )
                }.fold(
                    onSuccess = { RemoteKeyData(it, true) },
                    onFailure = {
                        if (it is UserKeyNotActive) {
                            RemoteKeyData(EncryptedByteArray(byteArrayOf()), false)
                        } else {
                            throw it
                        }
                    }
                )

                ShareKeyEntity(
                    rotation = response.keyRotation,
                    userId = userId.id,
                    addressId = addressId.id,
                    shareId = shareId.id,
                    key = response.key,
                    createTime = response.createTime,
                    symmetricallyEncryptedKey = keyData.encryptedKey,
                    userKeyId = response.userKeyId,
                    isActive = keyData.isActive
                )
            }
        }
    }

    private fun entityToDomain(entity: ShareKeyEntity): ShareKey =
        ShareKey(
            rotation = entity.rotation,
            key = entity.symmetricallyEncryptedKey,
            responseKey = entity.key,
            createTime = entity.createTime,
            isActive = entity.isActive,
            userKeyId = entity.userKeyId
        )

    private data class RemoteKeyData(
        val encryptedKey: EncryptedByteArray,
        val isActive: Boolean
    )
}

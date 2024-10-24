/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.crypto.ReencryptShareKey
import proton.android.pass.data.impl.crypto.ReencryptShareKeyInput
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
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
            val readyKeys = tryToReencryptLocalKeys(userId, localKeys)

            if (readyKeys.isNotEmpty()) {
                emit(readyKeys.map(::entityToDomain))
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

        return encryptionContextProvider.withEncryptionContextSuspendable {
            remoteKeys.map { response ->
                val keyData = reencryptKey(
                    context = this,
                    user = user,
                    input = ReencryptShareKeyInput(
                        key = response.key,
                        userKeyId = response.userKeyId
                    )
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

    private suspend fun tryToReencryptLocalKeys(userId: UserId, keys: List<ShareKeyEntity>): List<ShareKeyEntity> {

        // Separate active keys from inactive keys
        val activeKeys = keys.filter { it.isActive }
        val inactiveKeys = keys.filter { !it.isActive }

        // If there are no inactive keys, we're done
        if (inactiveKeys.isEmpty()) return activeKeys

        // There are inactive keys, try to open and reencrypt them
        PassLogger.d(TAG, "Trying to reencrypt ${inactiveKeys.size} keys")
        val user = userRepository.getUser(userId)
        val reencryptedKeyResults = encryptionContextProvider.withEncryptionContextSuspendable {
            inactiveKeys.map {
                val output = reencryptKey(
                    context = this,
                    user = user,
                    input = ReencryptShareKeyInput(
                        key = it.key,
                        userKeyId = it.userKeyId
                    )
                )

                if (output.isActive) {
                    it.copy(
                        isActive = true,
                        symmetricallyEncryptedKey = output.encryptedKey
                    )
                } else {
                    it
                }
            }
        }

        val reencryptedKeys = reencryptedKeyResults.filter { it.isActive }

        PassLogger.d(TAG, "Successfully reencrypted ${reencryptedKeys.size} keys")
        if (reencryptedKeys.isNotEmpty()) {
            localDataSource.storeShareKeys(reencryptedKeys)
        }

        return activeKeys + reencryptedKeys
    }

    private fun reencryptKey(
        context: EncryptionContext,
        user: User,
        input: ReencryptShareKeyInput
    ): ReencryptedKeyData = runCatching {
        reencryptShareKey(
            encryptionContext = context,
            user = user,
            input = input
        )
    }.fold(
        onSuccess = { ReencryptedKeyData(it, true) },
        onFailure = {
            if (it is UserKeyNotActive) {
                ReencryptedKeyData(EncryptedByteArray(byteArrayOf()), false)
            } else {
                throw it
            }
        }
    )

    private fun entityToDomain(entity: ShareKeyEntity): ShareKey = ShareKey(
        rotation = entity.rotation,
        key = entity.symmetricallyEncryptedKey,
        responseKey = entity.key,
        createTime = entity.createTime,
        isActive = entity.isActive,
        userKeyId = entity.userKeyId
    )

    private data class ReencryptedKeyData(
        val encryptedKey: EncryptedByteArray,
        val isActive: Boolean
    )

    companion object {
        private const val TAG = "ShareKeyRepositoryImpl"
    }
}

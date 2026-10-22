/*
 * Copyright (c) 2023-2026 Proton AG
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
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.GetGroupKeyDecryptor
import proton.android.pass.crypto.api.usecases.GetUserKeys
import proton.android.pass.data.impl.crypto.ReencryptGroupKeyInput
import proton.android.pass.data.impl.crypto.ReencryptKeyInput
import proton.android.pass.data.impl.crypto.ReencryptShareKey
import proton.android.pass.data.impl.crypto.ReencryptShareKeyInput
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ShareKeyRepositoryImpl @Inject constructor(
    private val reencryptShareKey: ReencryptShareKey,
    private val getUserKeys: GetUserKeys,
    private val getGroupKeyDecryptor: GetGroupKeyDecryptor,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val localDataSource: LocalShareKeyDataSource,
    private val remoteDataSource: RemoteShareKeyDataSource
) : ShareKeyRepository {

    override fun getShareKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        groupEmail: String?,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean
    ): Flow<List<ShareKey>> = flow {
        if (!forceRefresh) {
            val localKeys = localDataSource.getAllShareKeysForShare(userId, shareId).first()
            val readyKeys = tryToReencryptLocalKeys(userId, localKeys, groupEmail)

            if (readyKeys.isNotEmpty()) {
                emit(readyKeys.map(ShareKeyEntity::toDomain))
                return@flow
            }
        }

        val keys = requestRemoteKeys(userId, addressId, shareId, groupEmail)
        if (shouldStoreLocally) {
            localDataSource.storeShareKeys(keys)
        }

        emit(keys.map(ShareKeyEntity::toDomain))
    }

    override suspend fun saveShareKeys(shareKeyEntities: List<ShareKeyEntity>) {
        localDataSource.storeShareKeys(shareKeyEntities)
    }

    override fun getLatestKeyForShare(shareId: ShareId): Flow<ShareKey> =
        localDataSource.getLatestKeyForShare(shareId).map(ShareKeyEntity::toDomain)

    override fun getShareKeyForRotation(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        groupEmail: String?,
        keyRotation: Long
    ): Flow<ShareKey?> = flow {
        val localKeys = localDataSource.getAllShareKeysForShare(userId, shareId).first()
        val key = localKeys.firstOrNull { it.rotation == keyRotation }
        if (key != null) {
            val mapped = key.toDomain()
            emit(mapped)
            return@flow
        }

        // Key was not present, force a refresh
        val retrievedKeys = requestRemoteKeys(userId, addressId, shareId, groupEmail)
        localDataSource.storeShareKeys(retrievedKeys)

        val retrievedKey = retrievedKeys.firstOrNull { it.rotation == keyRotation }
        if (retrievedKey != null) {
            val mapped = retrievedKey.toDomain()
            emit(mapped)
        } else {
            emit(null)
        }
    }

    private suspend fun requestRemoteKeys(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        groupEmail: String?
    ): List<ShareKeyEntity> {
        val remoteKeys = remoteDataSource.getShareKeys(userId, shareId).first()
        PassLogger.i(
            TAG,
            "Requesting ${remoteKeys.size} remote key(s) for share ${shareId.id}, isGroupShare=${groupEmail != null}"
        )
        return encryptionContextProvider.withEncryptionContextSuspendable {
            remoteKeys.map { response ->
                safeRunCatching {
                    val input = if (groupEmail == null) {
                        createReencryptShareKeyInput(
                            userId = userId,
                            key = response.key,
                            userKeyId = response.userKeyId,
                            addressId = addressId.id
                        )
                    } else {
                        createReencryptGroupKeyInput(
                            userId = userId,
                            key = response.key,
                            addressId = addressId,
                            groupEmail = groupEmail
                        )
                    }
                    val keyData = reencryptKey(
                        context = this,
                        input = input
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
                }.onFailure { e ->
                    PassLogger.w(
                        TAG,
                        "Failed to process key rotation ${response.keyRotation} for share ${shareId.id} " +
                            "(isGroupShare=${groupEmail != null})"
                    )
                    PassLogger.w(TAG, e)
                }.getOrThrow()
            }
        }
    }

    private suspend fun tryToReencryptLocalKeys(
        userId: UserId,
        keys: List<ShareKeyEntity>,
        groupEmail: String?
    ): List<ShareKeyEntity> {

        // Separate active keys from inactive keys
        val activeKeys = keys.filter { it.isActive }
        val inactiveKeys = keys.filter { !it.isActive }

        // If there are no inactive keys, we're done
        if (inactiveKeys.isEmpty()) return activeKeys

        // There are inactive keys, try to open and reencrypt them
        PassLogger.d(TAG, "Trying to reencrypt ${inactiveKeys.size} keys")
        val reencryptedKeyResults = encryptionContextProvider.withEncryptionContextSuspendable {
            inactiveKeys.map {
                val input = if (groupEmail == null) {
                    createReencryptShareKeyInput(
                        userId = userId,
                        key = it.key,
                        userKeyId = it.userKeyId,
                        addressId = it.addressId
                    )
                } else {
                    createReencryptGroupKeyInput(
                        userId = userId,
                        key = it.key,
                        addressId = AddressId(it.addressId),
                        groupEmail = groupEmail
                    )
                }
                val output = reencryptKey(
                    context = this,
                    input = input
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

    private suspend fun createReencryptShareKeyInput(
        userId: UserId,
        key: String,
        userKeyId: String,
        addressId: String
    ): ReencryptShareKeyInput = ReencryptShareKeyInput(
        key = key,
        userKeyId = userKeyId,
        addressId = addressId,
        userKeys = getUserKeys(userId)
    )

    private suspend fun createReencryptGroupKeyInput(
        userId: UserId,
        key: String,
        addressId: AddressId,
        groupEmail: String
    ): ReencryptGroupKeyInput = ReencryptGroupKeyInput(
        key = key,
        decryptor = getGroupKeyDecryptor(userId, addressId, groupEmail)
    )

    private fun reencryptKey(context: EncryptionContext, input: ReencryptKeyInput): ReencryptedKeyData = runCatching {
        reencryptShareKey(
            encryptionContext = context,
            input = input
        )
    }.fold(
        onSuccess = { ReencryptedKeyData(it, true) },
        onFailure = {
            PassLogger.w(TAG, "Error reencrypting key: ${it::class.simpleName}")
            PassLogger.w(TAG, it)
            if (it is UserKeyNotActive) {
                ReencryptedKeyData(EncryptedByteArray(byteArrayOf()), false)
            } else {
                throw it
            }
        }
    )

    private data class ReencryptedKeyData(
        val encryptedKey: EncryptedByteArray,
        val isActive: Boolean
    )

    companion object {
        private const val TAG = "ShareKeyRepositoryImpl"
    }
}

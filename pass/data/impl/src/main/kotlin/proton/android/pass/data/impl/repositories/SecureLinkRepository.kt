/*
 * Copyright (c) 2024 Proton AG
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

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.crypto.GetItemKeys
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.securelink.SecureLinkOptions
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.local.securelinks.SecureLinksLocalDataSource
import proton.android.pass.data.impl.remote.RemoteSecureLinkDataSource
import proton.android.pass.data.impl.requests.CreateSecureLinkRequest
import proton.android.pass.data.impl.responses.GetSecureLinkResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface SecureLinkRepository {

    suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): SecureLinkId

    suspend fun deleteSecureLink(userId: UserId, secureLinkId: SecureLinkId)

    suspend fun deleteInactiveSecureLinks(userId: UserId)

    fun observeSecureLink(userId: UserId, secureLinkId: SecureLinkId): Flow<SecureLink>

    fun observeSecureLinks(userId: UserId): Flow<List<SecureLink>>

    fun observeSecureLinksCount(userId: UserId): Flow<Int>

}

class SecureLinkRepositoryImpl @Inject constructor(
    private val localItemDataSource: LocalItemDataSource,
    private val localShareKeyDataSource: LocalShareKeyDataSource,
    private val remoteSecureLinkDataSource: RemoteSecureLinkDataSource,
    private val secureLinksLocalDataSource: SecureLinksLocalDataSource,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val shareRepository: ShareRepository,
    private val getItemKeys: GetItemKeys
) : SecureLinkRepository {

    override suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): SecureLinkId {
        val item = localItemDataSource.getById(shareId, itemId) ?: throw IllegalStateException(
            "Item not found [shareId=${shareId.id}] [itemId=${itemId.id}]"
        )

        val userAddress = shareRepository.getAddressForShareId(userId, shareId)

        val (shareKey, itemKey) = getItemKeys(
            userAddress = userAddress,
            shareId = shareId,
            itemId = itemId
        )

        val decryptedItemKey = encryptionContextProvider.withEncryptionContext { decrypt(itemKey.key) }

        val linkEncryptionKey = EncryptionKey.generate()

        val encryptedItemKey = encryptionContextProvider.withEncryptionContext(linkEncryptionKey.clone()) {
            encrypt(decryptedItemKey, EncryptionTag.ItemKey)
        }

        val encodedEncryptedItemKey = Base64.encodeBase64String(encryptedItemKey.array)

        val shareEncryptionKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val encryptedLinkKey = encryptionContextProvider.withEncryptionContext(shareEncryptionKey) {
            encrypt(linkEncryptionKey.value(), EncryptionTag.LinkKey)
        }

        val encodedEncryptedLinkKey = Base64.encodeBase64String(encryptedLinkKey.array)

        val request = CreateSecureLinkRequest(
            revision = item.revision,
            expirationTime = options.expirationSeconds,
            maxReadCount = options.maxReadCount,
            encryptedItemKey = encodedEncryptedItemKey,
            encryptedLinkKey = encodedEncryptedLinkKey,
            linkKeyShareKeyRotation = shareKey.rotation
        )

        val response = remoteSecureLinkDataSource.createSecureLink(
            userId = userId,
            shareId = shareId,
            itemId = itemId,
            request = request
        )

        val encodedLinkKey = Base64.encodeBase64String(linkEncryptionKey.value(), Base64.Mode.UrlSafe)
        val concatenated = "${response.url}#$encodedLinkKey"

        secureLinksLocalDataSource.create(
            userId = userId,
            secureLink = SecureLink(
                id = SecureLinkId(response.secureLinkId),
                shareId = shareId,
                itemId = itemId,
                expirationInSeconds = response.expirationTime,
                isActive = true,
                maxReadCount = options.maxReadCount,
                readCount = 0,
                url = concatenated
            )
        )

        return SecureLinkId(id = response.secureLinkId)
    }

    override suspend fun deleteSecureLink(userId: UserId, secureLinkId: SecureLinkId) {
        runCatching { remoteSecureLinkDataSource.deleteSecureLink(userId, secureLinkId) }
            .onFailure { exception ->
                if (exception is ApiException) {
                    val error = exception.error
                    if (
                        !(error is ApiResult.Error.Http && error.proton?.code == SECURE_LINK_DOES_NOT_EXITS_ERROR_CODE)
                    ) {
                        throw exception
                    }
                } else {
                    throw exception
                }
            }

        secureLinksLocalDataSource.delete(userId, secureLinkId)
    }

    override suspend fun deleteInactiveSecureLinks(userId: UserId) {
        remoteSecureLinkDataSource.deleteInactiveSecureLinks(userId)
        secureLinksLocalDataSource.deleteAllInactive(userId)
    }

    override fun observeSecureLink(userId: UserId, secureLinkId: SecureLinkId): Flow<SecureLink> =
        secureLinksLocalDataSource.observe(userId, secureLinkId)

    override fun observeSecureLinks(userId: UserId): Flow<List<SecureLink>> = flow {
        val localSecureLinks = secureLinksLocalDataSource.getAll(userId)
        emit(localSecureLinks)

        runCatching { fetchSecureLinksFromRemote(userId) }
            .onFailure { error ->
                if (localSecureLinks.isEmpty()) {
                    throw error
                }
            }
            .onSuccess { remoteSecureLinks ->
                syncSecureLinks(userId, remoteSecureLinks)
            }

        emitAll(secureLinksLocalDataSource.observeAll(userId))
    }

    override fun observeSecureLinksCount(userId: UserId): Flow<Int> = flow {
        emit(secureLinksLocalDataSource.getCount(userId))

        runCatching { fetchSecureLinksFromRemote(userId) }
            .onSuccess { remoteSecureLinks ->
                syncSecureLinks(userId, remoteSecureLinks)
            }

        emitAll(secureLinksLocalDataSource.observeCount(userId))
    }

    private suspend fun syncSecureLinks(userId: UserId, remoteSecureLinks: List<SecureLink>) {
        remoteSecureLinks.map { remoteSecureLink ->
            remoteSecureLink.id
        }.let { remoteSecureLinksIds ->
            secureLinksLocalDataSource.getAll(userId).filterNot { localSecureLink ->
                remoteSecureLinksIds.contains(localSecureLink.id)
            }
        }.also { secureLinksToBeRemoved ->
            secureLinksLocalDataSource.delete(userId, secureLinksToBeRemoved)
            try {
                secureLinksLocalDataSource.update(userId, remoteSecureLinks)
            } catch (exception: SQLiteConstraintException) {
                // this is to avoid raising an error to a user due to a race condition
                // while secure link removal is propagated to backend database replicas
                PassLogger.w(TAG, "Error syncing remote secure links")
                PassLogger.w(TAG, exception)
            }
        }
    }

    private suspend fun fetchSecureLinksFromRemote(userId: UserId): List<SecureLink> {
        val remoteLinks = remoteSecureLinkDataSource.getAllSecureLinks(userId)

        // Retrieve all the ShareKeys we'll need to decrypt the SecureLinks
        val shareKeys = getAllShareKeysForShares(userId, remoteLinks)

        val mapped = remoteLinks.mapNotNull { link ->
            val shareKey = shareKeys[ShareId(link.shareId)]
                ?: return@mapNotNull null

            val linkKey = encryptionContextProvider.withEncryptionContext(shareKey.clone()) {
                val encryptedLinkKey = Base64.decodeBase64(link.encryptedLinkKey)
                decrypt(EncryptedByteArray(encryptedLinkKey), EncryptionTag.LinkKey)
            }

            val encodedLinkKey = Base64.encodeBase64String(linkKey, Base64.Mode.UrlSafe)
            val fullUrl = "${link.linkUrl}#$encodedLinkKey"

            SecureLink(
                id = SecureLinkId(link.linkId),
                shareId = ShareId(link.shareId),
                itemId = ItemId(link.itemId),
                expirationInSeconds = link.expirationTime,
                isActive = link.isActive,
                maxReadCount = link.maxReadCount,
                readCount = link.readCount,
                url = fullUrl
            )
        }

        // Clear all encryption keys from memory
        shareKeys.values.forEach { it.clear() }

        return mapped
    }

    private suspend fun getAllShareKeysForShares(
        userId: UserId,
        secureLinks: List<GetSecureLinkResponse>
    ): Map<ShareId, EncryptionKey> {
        val shareKeyRequests = secureLinks.map { ShareKeyForLink.fromResponse(it) }.distinct()

        val res: Map<ShareId, EncryptionKey?> = encryptionContextProvider
            .withEncryptionContextSuspendable {
                coroutineScope {
                    shareKeyRequests.map { request ->
                        async {
                            val shareKey = localShareKeyDataSource
                                .getForShareAndRotation(
                                    userId = userId,
                                    shareId = request.shareId,
                                    rotation = request.rotation
                                )
                                .firstOrNull()
                                ?.let { shareKeyEntity ->
                                    EncryptionKey(decrypt(shareKeyEntity.symmetricallyEncryptedKey))
                                }
                            request.shareId to shareKey
                        }
                    }.awaitAll().toMap()
                }
            }

        return res.filterValues { it != null }.mapValues { it.value!! }
    }

    private data class ShareKeyForLink(
        val shareId: ShareId,
        val rotation: Long
    ) {
        companion object {
            fun fromResponse(response: GetSecureLinkResponse) = ShareKeyForLink(
                ShareId(response.shareId),
                response.linkKeyShareKeyRotation
            )
        }
    }

    private companion object {

        private const val TAG = "SecureLinkRepository"

        private const val SECURE_LINK_DOES_NOT_EXITS_ERROR_CODE = 2001

    }

}

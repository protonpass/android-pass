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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.primary
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.UpdateShareEvent
import proton.android.pass.data.impl.crypto.ReencryptShareContents
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toProto
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.toResponse
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.domain.key.ShareKey
import proton.android.pass.log.api.PassLogger
import proton_pass_vault_v1.VaultV1
import java.sql.Date
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val remoteShareDataSource: RemoteShareDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val reencryptShareContents: ReencryptShareContents,
    private val createVault: CreateVault,
    private val updateVault: UpdateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val shareKeyRepository: ShareKeyRepository
) : ShareRepository {

    override suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): Share = withContext(Dispatchers.IO) {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val user = requireNotNull(userRepository.getUser(userId))
        val userPrimaryKey = requireNotNull(user.keys.primary()?.keyId?.id)

        val (request, shareKey) = runCatching {
            createVaultRequest(user, vault, userAddress)
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, "Error in CreateVaultRequest")
                PassLogger.e(TAG, it)
                throw it
            }
        )

        val createVaultResponse = remoteShareDataSource.createVault(userAddress.userId, request)
        val symmetricallyEncryptedKey = encryptionContextProvider.withEncryptionContext {
            encrypt(shareKey.value())
        }
        val responseAsEntity = shareResponseToEntity(
            userAddress = userAddress,
            shareResponse = createVaultResponse,
            key = EncryptionKeyStatus.Found(shareKey)
        )

        val shareKeyEntity = ShareKeyEntity(
            rotation = 1,
            userId = userId.id,
            addressId = userAddress.addressId.id,
            shareId = createVaultResponse.shareId,
            key = request.encryptedVaultKey,
            createTime = createVaultResponse.createTime,
            symmetricallyEncryptedKey = symmetricallyEncryptedKey,
            isActive = true,
            userKeyId = userPrimaryKey
        )
        database.inTransaction("createVault") {
            localShareDataSource.upsertShares(listOf(responseAsEntity))
            shareKeyRepository.saveShareKeys(listOf(shareKeyEntity))
        }

        return@withContext shareEntityToShare(responseAsEntity)
    }

    override suspend fun deleteVault(userId: UserId, shareId: ShareId) {
        withContext(Dispatchers.IO) {
            remoteShareDataSource.deleteVault(userId, shareId)
            localShareDataSource.deleteShares(setOf(shareId))
        }
    }

    override fun observeAllShares(userId: SessionUserId): Flow<List<Share>> =
        localShareDataSource.observeAllActiveSharesForUser(userId)
            .map { shares ->
                encryptionContextProvider.withEncryptionContext {
                    shares.map { share -> shareEntityToShare(share, this) }
                }
            }

    @Suppress("LongMethod")
    override suspend fun refreshShares(userId: UserId): RefreshSharesResult =
        withContext(Dispatchers.IO) {
            PassLogger.i(TAG, "Refreshing shares")
            val userAddress = userAddressRepository.getAddresses(userId).primary()
                ?: throw IllegalStateException("Could not find PrimaryAddress")
            PassLogger.i(TAG, "Found primary user address")

            // Retrieve remote shares and create a map ShareId->ShareResponse
            val remoteSharesDeferred: Deferred<List<ShareResponse>> =
                async { remoteShareDataSource.getShares(userAddress.userId) }
            remoteSharesDeferred.invokeOnCompletion {
                if (it != null) {
                    PassLogger.w(TAG, it)
                } else {
                    PassLogger.i(TAG, "Fetched remote shares")
                }
            }
            // Retrieve local shares and create a map ShareId->ShareEntity
            val localSharesDeferred: Deferred<List<ShareEntity>> = async {
                localShareDataSource
                    .getAllSharesForUser(userAddress.userId)
                    .first()
            }
            localSharesDeferred.invokeOnCompletion {
                if (it != null) {
                    PassLogger.w(TAG, it)
                } else {
                    PassLogger.i(TAG, "Retrieved local shares")
                }
            }

            val sources = awaitAll(remoteSharesDeferred, localSharesDeferred)
            val remoteShares = sources[0].filterIsInstance<ShareResponse>()
            PassLogger.i(TAG, "Fetched ${remoteShares.size} remote shares")

            val localShares = sources[1].filterIsInstance<ShareEntity>()
            PassLogger.i(TAG, "Retrieved ${localShares.size} local shares")
            val remoteShareMap = remoteShares.associateBy { ShareId(it.shareId) }
            val localSharesMap = localShares.associateBy { ShareId(it.id) }

            // Update local share if needed
            val sharesToUpdate = remoteShareMap.mapNotNull { (_, remoteShare) ->
                val localShare = localSharesMap[ShareId(remoteShare.shareId)]
                if (localShare != null && localShareNeedsUpdate(localShare, remoteShare)) {
                    localShare to remoteShare
                } else {
                    null
                }
            }.map { (localShare, remoteShare) ->
                localShare.copy(
                    owner = remoteShare.owner,
                    shareRoleId = remoteShare.shareRoleId,
                    targetMembers = remoteShare.targetMembers,
                    shared = remoteShare.shared,
                    permission = remoteShare.permission,
                    targetMaxMembers = remoteShare.targetMaxMembers,
                    expirationTime = remoteShare.expirationTime,
                    newUserInvitesReady = remoteShare.newUserInvitesReady,
                    pendingInvites = remoteShare.pendingInvites
                )
            }

            // Delete from the local data source the shares that are not in remote response
            val toDelete = localSharesMap.keys.subtract(remoteShareMap.keys)

            if (sharesToUpdate.isNotEmpty() || toDelete.isNotEmpty()) {
                database.inTransaction("refreshShares") {
                    if (sharesToUpdate.isNotEmpty()) {
                        PassLogger.i(TAG, "Updating ${sharesToUpdate.size} shares")
                        localShareDataSource.upsertShares(sharesToUpdate)
                    }
                    if (toDelete.isNotEmpty()) {
                        PassLogger.i(TAG, "Deleting ${toDelete.size} shares")
                        val deletedShareResult = localShareDataSource.deleteShares(toDelete)
                        PassLogger.i(TAG, "Deleted $deletedShareResult shares")
                    }
                }
            }

            val sharesNotInLocal = remoteShares
                // Filter out shares that are not in the local storage
                .filterNot { localSharesMap.containsKey(ShareId(it.shareId)) }
                .map { ShareId(it.shareId) }

            val inactiveShares = localShares.filter { !it.isActive }.map { ShareId(it.id) }

            val remoteSharesToSave = remoteShares.filter {
                sharesNotInLocal.contains(ShareId(it.shareId)) || inactiveShares.contains(ShareId(it.shareId))
            }
            val storedShares = storeShares(userAddress, remoteSharesToSave)

            val newShares = storedShares.filter {
                it.isActive && sharesNotInLocal.contains(ShareId(it.id))
            }

            val allShareIds = remoteShareMap.keys.filterNot { inactiveShares.contains(it) }.toSet()

            PassLogger.i(TAG, "Refreshed shares")
            RefreshSharesResult(
                allShareIds = allShareIds,
                newShareIds = newShares.map { ShareId(it.id) }.toSet()
            )
        }

    @Suppress("ReturnCount")
    override suspend fun getById(userId: UserId, shareId: ShareId): Share {
        // Check local
        var share: ShareEntity? = localShareDataSource.getById(userId, shareId)
        if (share == null) {
            // Check remote
            val fetchedShare = remoteShareDataSource.fetchShareById(userId, shareId)
            val shareResponse = fetchedShare ?: throw ShareNotAvailableError()
            val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
            val storedShares: List<ShareEntity> = storeShares(
                userAddress = userAddress,
                shares = listOf(shareResponse)
            )
            share = storedShares.first()
        }

        return shareEntityToShare(share)
    }

    override fun observeById(userId: UserId, shareId: ShareId): Flow<Option<Share>> {
        return localShareDataSource.observeById(userId, shareId)
            .map { entity ->
                entity.toOption().map {
                    shareEntityToShare(it)
                }
            }
    }

    override suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        vault: NewVault
    ): Share = withContext(Dispatchers.IO) {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

        val shareKey = shareKeyRepository.getLatestKeyForShare(shareId).first()
        val body = newVaultToBody(vault)
        val request = updateVault.createUpdateVaultRequest(shareKey, body).toRequest()
        val response = kotlin.runCatching {
            remoteShareDataSource.updateVault(userId, shareId, request)
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.w(TAG, "Error in updateVault")
                PassLogger.w(TAG, it)
                throw it
            }
        )

        val shareKeyAsEncryptionkey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }
        val responseAsEntity = shareResponseToEntity(
            userAddress = userAddress,
            shareResponse = response,
            key = EncryptionKeyStatus.Found(shareKeyAsEncryptionkey)
        )
        localShareDataSource.upsertShares(listOf(responseAsEntity))

        return@withContext shareEntityToShare(responseAsEntity)
    }

    override suspend fun deleteSharesForUser(userId: UserId) = withContext(Dispatchers.IO) {
        localShareDataSource.deleteSharesForUser(userId)
    }

    override fun observeVaultCount(userId: UserId): Flow<Int> =
        localShareDataSource.observeActiveVaultCount(userId)

    override suspend fun leaveVault(userId: UserId, shareId: ShareId) {
        remoteShareDataSource.leaveVault(userId, shareId)
        localShareDataSource.deleteShares(setOf(shareId))
    }

    override suspend fun applyUpdateShareEvent(
        userId: UserId,
        shareId: ShareId,
        event: UpdateShareEvent
    ) {
        val asResponse = event.toResponse()
        val userAddress = userAddressRepository.getAddress(userId, AddressId(asResponse.addressId))
            ?: return

        storeShares(userAddress, listOf(asResponse))
    }

    private suspend fun storeShares(
        userAddress: UserAddress,
        shares: List<ShareResponse>
    ): List<ShareEntity> = withContext(Dispatchers.IO) {
        PassLogger.i(TAG, "Fetching ShareKeys for ${shares.size} shares")
        val entities: List<Pair<ShareResponseEntity, List<ShareKey>>> = shares.map { response ->
            getShareKeys(response, userAddress)
        }.awaitAll()

        val shareEntities = entities.map { it.first.entity }
        val shareKeyEntities = entities
            .map { responsePair ->
                responsePair.second.map { shareKey ->
                    ShareKeyEntity(
                        rotation = shareKey.rotation,
                        userId = userAddress.userId.id,
                        addressId = userAddress.addressId.id,
                        shareId = responsePair.first.response.shareId,
                        key = shareKey.responseKey,
                        createTime = shareKey.createTime,
                        symmetricallyEncryptedKey = shareKey.key,
                        isActive = shareKey.isActive,
                        userKeyId = shareKey.userKeyId
                    )
                }
            }
            .flatten()


        if (shareEntities.isNotEmpty() || shareKeyEntities.isNotEmpty()) {
            database.inTransaction("storeShares") {
                // First, store the shares
                if (shareEntities.isNotEmpty()) {
                    PassLogger.i(TAG, "Storing ${shareEntities.size} shares")
                    localShareDataSource.upsertShares(shareEntities)
                }
                // Now that we have inserted the shares, we can safely insert the shareKeys
                if (shareKeyEntities.isNotEmpty()) {
                    PassLogger.i(TAG, "Storing ${shareKeyEntities.size} ShareKeys")
                    shareKeyRepository.saveShareKeys(shareKeyEntities)
                }
            }
        }

        shareEntities
    }

    private fun CoroutineScope.getShareKeys(
        shareResponse: ShareResponse,
        userAddress: UserAddress
    ): Deferred<Pair<ShareResponseEntity, List<ShareKey>>> = async {
        val shareId = ShareId(shareResponse.shareId)

        // First we fetch the shareKeys and not save them, in case the share has not been
        // inserted yet, as it would cause a FK mismatch in the database
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userAddress.userId,
            addressId = userAddress.addressId,
            shareId = shareId,
            forceRefresh = false,
            shouldStoreLocally = false
        ).first()

        // Reencrypt the share contents
        val encryptionKey =
            getEncryptionKey(shareResponse.contentKeyRotation, shareKeys)
        ShareResponseEntity(
            response = shareResponse,
            entity = shareResponseToEntity(
                userAddress,
                shareResponse,
                encryptionKey
            )
        ) to shareKeys
    }

    private fun getEncryptionKey(keyRotation: Long?, keys: List<ShareKey>): EncryptionKeyStatus {
        if (keyRotation == null) return EncryptionKeyStatus.NotFound

        val encryptionKey =
            keys.firstOrNull { it.rotation == keyRotation } ?: return EncryptionKeyStatus.NotFound
        if (!encryptionKey.isActive) {
            PassLogger.d(TAG, "Found key but it is not active")
            return EncryptionKeyStatus.Inactive
        }

        val decrypted =
            encryptionContextProvider.withEncryptionContext { decrypt(encryptionKey.key) }
        return EncryptionKeyStatus.Found(EncryptionKey(decrypted))
    }

    private fun shareResponseToEntity(
        userAddress: UserAddress,
        shareResponse: ShareResponse,
        key: EncryptionKeyStatus
    ): ShareEntity {

        val (encryptedContent, isActive) = when (key) {
            EncryptionKeyStatus.NotFound -> null to true
            is EncryptionKeyStatus.Found -> {
                reencryptShareContents(shareResponse.content, key.encryptionKey) to true
            }

            EncryptionKeyStatus.Inactive -> null to false
        }
        return ShareEntity(
            id = shareResponse.shareId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            vaultId = shareResponse.vaultId,
            targetType = shareResponse.targetType,
            targetId = shareResponse.targetId,
            permission = shareResponse.permission,
            content = shareResponse.content,
            contentKeyRotation = shareResponse.contentKeyRotation,
            contentFormatVersion = shareResponse.contentFormatVersion,
            expirationTime = shareResponse.expirationTime,
            createTime = shareResponse.createTime,
            encryptedContent = encryptedContent,
            isActive = isActive,
            shareRoleId = shareResponse.shareRoleId,
            owner = shareResponse.owner,
            targetMembers = shareResponse.targetMembers,
            shared = shareResponse.shared,
            targetMaxMembers = shareResponse.targetMaxMembers,
            newUserInvitesReady = shareResponse.newUserInvitesReady,
            pendingInvites = shareResponse.pendingInvites
        )
    }

    private fun shareEntityToShare(
        entity: ShareEntity,
        encryptionContext: EncryptionContext? = null
    ): Share {
        val shareType = ShareType.map[entity.targetType]
        if (shareType == null) {
            val e = IllegalStateException("Unknown ShareType")
            PassLogger.w(TAG, "Unknown ShareType [shareType=${entity.targetType}]")
            PassLogger.w(TAG, e)
            throw e
        }

        val (color, icon) = if (entity.encryptedContent == null) {
            ShareColor.Color1 to ShareIcon.Icon1
        } else {
            if (encryptionContext != null) {
                val decrypted = encryptionContext.decrypt(entity.encryptedContent)
                val asProto = VaultV1.Vault.parseFrom(decrypted)
                asProto.display.color.toDomain() to asProto.display.icon.toDomain()
            } else {
                encryptionContextProvider.withEncryptionContext {
                    val decrypted = decrypt(entity.encryptedContent)
                    val asProto = VaultV1.Vault.parseFrom(decrypted)
                    asProto.display.color.toDomain() to asProto.display.icon.toDomain()
                }
            }
        }

        return Share(
            id = ShareId(entity.id),
            shareType = shareType,
            targetId = entity.targetId,
            permission = SharePermission(entity.permission),
            vaultId = VaultId(entity.vaultId),
            content = entity.encryptedContent.toOption(),
            expirationTime = entity.expirationTime?.let { Date(it) },
            createTime = Date(entity.createTime),
            color = color,
            icon = icon,
            shareRole = ShareRole.fromValue(entity.shareRoleId),
            isOwner = entity.owner,
            memberCount = entity.targetMembers,
            shared = entity.shared,
            pendingInvites = entity.pendingInvites,
            newUserInvitesReady = entity.newUserInvitesReady,
            maxMembers = entity.targetMaxMembers
        )
    }

    private fun createVaultRequest(
        user: User,
        vault: NewVault,
        userAddress: UserAddress
    ): Pair<CreateVaultRequest, EncryptionKey> {
        val body = newVaultToBody(vault)
        val (request, shareKey) = createVault.createVaultRequest(user, userAddress, body)
        return request.toRequest() to shareKey
    }

    private fun newVaultToBody(vault: NewVault): VaultV1.Vault {
        val (name, description) = encryptionContextProvider.withEncryptionContext {
            decrypt(vault.name) to decrypt(vault.description)
        }

        val display = VaultV1.VaultDisplayPreferences.newBuilder()
            .setColor(vault.color.toProto())
            .setIcon(vault.icon.toProto())
            .build()
        return VaultV1.Vault.newBuilder()
            .setName(name)
            .setDescription(description)
            .setDisplay(display)
            .build()
    }

    private fun localShareNeedsUpdate(
        localShare: ShareEntity,
        remoteShare: ShareResponse
    ): Boolean = when {
        localShare.owner != remoteShare.owner -> true
        localShare.shareRoleId != remoteShare.shareRoleId -> true
        localShare.permission != remoteShare.permission -> true
        localShare.targetMembers != remoteShare.targetMembers -> true
        localShare.shared != remoteShare.shared -> true
        localShare.targetMaxMembers != remoteShare.targetMaxMembers -> true
        localShare.expirationTime != remoteShare.expirationTime -> true
        localShare.pendingInvites != remoteShare.pendingInvites -> true
        localShare.newUserInvitesReady != remoteShare.newUserInvitesReady -> true

        else -> false
    }

    internal data class ShareResponseEntity(
        val response: ShareResponse,
        val entity: ShareEntity
    )

    internal sealed interface EncryptionKeyStatus {
        object NotFound : EncryptionKeyStatus
        data class Found(val encryptionKey: EncryptionKey) : EncryptionKeyStatus
        object Inactive : EncryptionKeyStatus
    }

    companion object {
        private const val TAG = "ShareRepositoryImpl"
    }
}

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.primary
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.impl.crypto.ReencryptShareContents
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.extensions.toProto
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Share
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermission
import proton.pass.domain.ShareType
import proton.pass.domain.VaultId
import proton.pass.domain.entity.NewVault
import proton.pass.domain.key.ShareKey
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

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
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
                PassLogger.w(TAG, it, "Error in CreateVaultRequest")
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
        database.inTransaction {
            localShareDataSource.upsertShares(listOf(responseAsEntity))
            shareKeyRepository.saveShareKeys(listOf(shareKeyEntity))
        }

        return@withContext this@ShareRepositoryImpl.shareEntityToShare(responseAsEntity)
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
                shares.map { shareEntityToShare(it) }
            }

    override suspend fun refreshShares(userId: UserId): RefreshSharesResult =
        withContext(Dispatchers.IO) {
            val userAddress = userAddressRepository.getAddresses(userId).primary()
                ?: throw IllegalStateException("Could not find PrimaryAddress")

            // Retrieve remote shares and create a map ShareId->ShareResponse
            val remoteShares = remoteShareDataSource.getShares(userAddress.userId)
            val remoteShareMap = remoteShares.associateBy { ShareId(it.shareId) }

            val (toSave, inactiveShares) = database.inTransaction {

                // Retrieve local shares and create a map ShareId->ShareEntity
                val localShares = localShareDataSource
                    .getAllSharesForUser(userAddress.userId)
                    .first()
                val localSharesMap = localShares.associateBy { ShareId(it.id) }

                // Update primary status if needed
                remoteShareMap.forEach { (remoteId, remoteShare) ->
                    val localShare = localSharesMap[remoteId]
                    if (localShare != null && localShare.isPrimary != remoteShare.primary) {
                        localShareDataSource.setPrimaryShareStatus(
                            userId = userId,
                            shareId = remoteId,
                            isPrimary = remoteShare.primary
                        )
                    }
                }

                // Delete from the local data source the shares that are not in remote response
                val toDelete = localSharesMap.keys.subtract(remoteShareMap.keys)
                localShareDataSource.deleteShares(toDelete)

                val sharesNotInLocal = remoteShares

                    // Filter out shares that are not in the local storage
                    .filterNot { localSharesMap.containsKey(ShareId(it.shareId)) }
                    .map { ShareId(it.shareId) }

                val inactiveShares = localShares.filter { !it.isActive }.map { ShareId(it.id) }

                // Return shares that were not in local, so they are saved, and also the shares
                // that were marked as inactive, so we can detect if they can be active again
                sharesNotInLocal to inactiveShares
            }

            val remoteSharesToSave = remoteShares.filter {
                toSave.contains(ShareId(it.shareId)) || inactiveShares.contains(ShareId(it.shareId))
            }
            val storedShares = storeShares(userAddress, remoteSharesToSave)

            val newShares = storedShares.filter {
                it.isActive && toSave.contains(ShareId(it.id))
            }

            val allShareIds = remoteShareMap.keys.filterNot { inactiveShares.contains(it) }.toSet()

            RefreshSharesResult(
                allShareIds = allShareIds,
                newShareIds = newShares.map { ShareId(it.id) }.toSet()
            )
        }

    @Suppress("ReturnCount")
    override suspend fun getById(userId: UserId, shareId: ShareId): Share =
        withContext(Dispatchers.IO) {
            val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

            // Check local
            var share: ShareEntity? = localShareDataSource.getById(userId, shareId)
            if (share == null) {
                // Check remote
                val fetchedShare = remoteShareDataSource.fetchShareById(userId, shareId)
                val shareResponse = fetchedShare ?: throw ShareNotAvailableError()

                val storedShares: List<ShareEntity> = storeShares(
                    userAddress = userAddress,
                    shares = listOf(shareResponse)
                )
                share = storedShares.first()
            }

            return@withContext this@ShareRepositoryImpl.shareEntityToShare(share)
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
                PassLogger.w(TAG, it, "Error in updateVault")
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

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId): Share =
        withContext(Dispatchers.IO) {
            remoteShareDataSource.markAsPrimary(userId, shareId)

            val updated = database.inTransaction {
                val share = localShareDataSource.getById(userId, shareId)
                    ?: throw IllegalStateException("Could not find share with id $shareId")
                localShareDataSource.disablePrimaryShare(userId)

                val updatedShare = share.copy(isPrimary = true)
                localShareDataSource.upsertShares(listOf(updatedShare))
                updatedShare
            }

            return@withContext shareEntityToShare(updated)
        }

    override suspend fun deleteSharesForUser(userId: UserId) = withContext(Dispatchers.IO) {
        localShareDataSource.deleteSharesForUser(userId)
    }

    override fun observeVaultCount(userId: UserId): Flow<Int> =
        localShareDataSource.observeActiveVaultCount(userId)

    private suspend fun storeShares(
        userAddress: UserAddress,
        shares: List<ShareResponse>
    ): List<ShareEntity> {
        val entities: List<Pair<ShareResponseEntity, List<ShareKey>>> =
            shares.map { shareResponse ->
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
                val encryptionKey = getEncryptionKey(shareResponse.contentKeyRotation, shareKeys)
                ShareResponseEntity(
                    response = shareResponse,
                    entity = shareResponseToEntity(userAddress, shareResponse, encryptionKey)
                ) to shareKeys
            }

        return database.inTransaction {
            // First, store the shares
            val shareEntities = entities.map { it.first.entity }
            localShareDataSource.upsertShares(shareEntities)

            // Now that we have inserted the shares, we can safely insert the shareKeys
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
            shareKeyRepository.saveShareKeys(shareKeyEntities)

            shareEntities
        }
    }

    private fun getEncryptionKey(keyRotation: Long?, keys: List<ShareKey>): EncryptionKeyStatus {
        if (keyRotation == null) return EncryptionKeyStatus.NotFound

        val encryptionKey = keys.firstOrNull { it.rotation == keyRotation } ?: return EncryptionKeyStatus.NotFound
        if (!encryptionKey.isActive) {
            PassLogger.d(TAG, "Found key but it is not active")
            return EncryptionKeyStatus.Inactive
        }

        val decrypted = encryptionContextProvider.withEncryptionContext { decrypt(encryptionKey.key) }
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
            isPrimary = shareResponse.primary,
            isActive = isActive
        )
    }

    private fun shareEntityToShare(entity: ShareEntity): Share {
        val shareType = ShareType.map[entity.targetType]
        if (shareType == null) {
            val e = IllegalStateException("Unknown ShareType")
            PassLogger.w(TAG, e, "Unknown ShareType [shareType=${entity.targetType}]")
            throw e
        }

        val (color, icon) = if (entity.encryptedContent == null) {
            ShareColor.Color1 to ShareIcon.Icon1
        } else {
            encryptionContextProvider.withEncryptionContext {
                val decrypted = decrypt(entity.encryptedContent)
                val asProto = VaultV1.Vault.parseFrom(decrypted)
                asProto.display.color.toDomain() to asProto.display.icon.toDomain()
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
            isPrimary = entity.isPrimary
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

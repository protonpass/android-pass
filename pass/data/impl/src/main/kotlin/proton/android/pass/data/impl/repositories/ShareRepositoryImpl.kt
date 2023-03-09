package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import org.apache.commons.codec.binary.Base64
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.data.api.errors.CannotDeleteCurrentVaultError
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
    private val encryptionContextProvider: EncryptionContextProvider,
    private val shareKeyRepository: ShareKeyRepository
) : ShareRepository {

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): LoadingResult<Share> = withContext(Dispatchers.IO) {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val user = requireNotNull(userRepository.getUser(userId))

        val (request, shareKey) = try {
            createVaultRequest(user, vault, userAddress)
        } catch (e: RuntimeException) {
            PassLogger.w(TAG, e, "Error in CreateVaultRequest")
            return@withContext LoadingResult.Error(e)
        }

        val createVaultResult = remoteShareDataSource.createVault(userAddress.userId, request)
        val createVaultResponse = when (createVaultResult) {
            is LoadingResult.Error -> return@withContext LoadingResult.Error(createVaultResult.exception)
            LoadingResult.Loading -> return@withContext LoadingResult.Loading
            is LoadingResult.Success -> createVaultResult.data
        }

        val responseAsEntity = shareResponseToEntity(userAddress, createVaultResponse, shareKey)
        val symmetricallyEncryptedKey = encryptionContextProvider.withEncryptionContext {
            encrypt(shareKey.key)
        }

        val shareKeyEntity = ShareKeyEntity(
            rotation = 1,
            userId = userId.id,
            addressId = userAddress.addressId.id,
            shareId = createVaultResponse.shareId,
            key = Base64.encodeBase64String(shareKey.key),
            createTime = createVaultResponse.createTime,
            symmetricallyEncryptedKey = symmetricallyEncryptedKey
        )
        database.inTransaction {
            localShareDataSource.upsertShares(listOf(responseAsEntity))
            shareKeyRepository.saveShareKeys(listOf(shareKeyEntity))
        }

        return@withContext shareEntityToShare(responseAsEntity)
    }

    override suspend fun deleteVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            database.inTransaction {
                val currentSelectedShare: ShareEntity =
                    localShareDataSource.getSelectedSharesForUser(userId = userId)
                        .first()
                        .first()
                if (currentSelectedShare.id == shareId.id)
                    return@inTransaction LoadingResult.Error(CannotDeleteCurrentVaultError())
                remoteShareDataSource.deleteVault(userId, shareId)
                    .map { localShareDataSource.deleteShare(shareId) }
                    .map { }
            }
        }

    override suspend fun selectVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            localShareDataSource.updateSelectedShare(shareId)
            LoadingResult.Success(Unit)
        }

    override fun observeAllShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>> =
        localShareDataSource.getAllSharesForUser(userId).toShare()

    override fun observeSelectedShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>> =
        localShareDataSource.getSelectedSharesForUser(userId).toShare()

    private fun Flow<List<ShareEntity>>.toShare(): Flow<LoadingResult<List<Share>>> =
        this.map { LoadingResult.Success(it) }
            .mapLatest { sharesResult ->
                if (sharesResult.data.isEmpty()) return@mapLatest LoadingResult.Success(emptyList<Share>())
                shareEntitiesToShares(sharesResult.data)
            }
            .flowOn(Dispatchers.IO)

    override suspend fun refreshShares(userId: UserId): LoadingResult<List<Share>> =
        withContext(Dispatchers.IO) {
            return@withContext when (val sharesResult = performShareRefresh(userId)) {
                is LoadingResult.Error -> LoadingResult.Error(sharesResult.exception)
                LoadingResult.Loading -> LoadingResult.Loading
                is LoadingResult.Success -> shareEntitiesToShares(sharesResult.data)
            }
        }

    @Suppress("ReturnCount")
    override suspend fun getById(userId: UserId, shareId: ShareId): LoadingResult<Share?> =
        withContext(Dispatchers.IO) {
            val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

            // Check local
            var share: ShareEntity? = localShareDataSource.getById(userId, shareId)
            if (share == null) {
                // Check remote
                val getShareResult = remoteShareDataSource.getShareById(userId, shareId)
                when (getShareResult) {
                    is LoadingResult.Error -> return@withContext LoadingResult.Error(getShareResult.exception)
                    LoadingResult.Loading -> return@withContext LoadingResult.Loading
                    is LoadingResult.Success -> Unit
                }
                val shareResponse = getShareResult.data
                    ?: return@withContext LoadingResult.Error(IllegalStateException("Share Response is null"))

                val storedShares: List<ShareEntity> =
                    storeShares(userAddress, false, listOf(shareResponse))
                share = storedShares.first()
            }

            return@withContext shareEntityToShare(share)
        }

    @Suppress("ReturnCount")
    private suspend fun performShareRefresh(userId: UserId): LoadingResult<List<ShareEntity>> {
        val userAddress = userAddressRepository.getAddresses(userId).primary()
        if (userAddress == null) {
            val e = IllegalStateException("Could not find PrimaryAddress")
            PassLogger.w(TAG, e, "Error in performShareRefresh")
            return LoadingResult.Error(e)
        }

        val sharesResult = remoteShareDataSource.getShares(userAddress.userId)
        when (sharesResult) {
            is LoadingResult.Error -> return LoadingResult.Error(sharesResult.exception)
            LoadingResult.Loading -> return LoadingResult.Loading
            is LoadingResult.Success -> Unit
        }
        return LoadingResult.Success(storeShares(userAddress, true, sharesResult.data))
    }

    @Suppress("ReturnCount")
    private fun shareEntitiesToShares(entities: List<ShareEntity>): LoadingResult<List<Share>> {
        val mapped = entities.map {
            when (val res = shareEntityToShare(it)) {
                is LoadingResult.Error -> return LoadingResult.Error(res.exception)
                LoadingResult.Loading -> return LoadingResult.Loading
                is LoadingResult.Success -> res.data
            }
        }
        return LoadingResult.Success(mapped)
    }

    private suspend fun storeShares(
        userAddress: UserAddress,
        cleanUp: Boolean,
        shares: List<ShareResponse>
    ): List<ShareEntity> {
        val entities: List<Pair<ShareResponseEntity, List<ShareKey>>> = shares.map { shareResponse ->
            val shareId = ShareId(shareResponse.shareId)

            // First we fetch the shareKeys and not save them, in case the share has not been
            // inserted yet, as it would cause a FK mismatch in teh database
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
            if (cleanUp) {
                localShareDataSource.evictAndUpsertShares(userAddress.userId, shareEntities)
            } else {
                localShareDataSource.upsertShares(shareEntities)
            }

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
                            symmetricallyEncryptedKey = shareKey.key
                        )
                    }
                }
                .flatten()
            shareKeyRepository.saveShareKeys(shareKeyEntities)

            shareEntities
        }
    }

    private fun getEncryptionKey(keyRotation: Long?, keys: List<ShareKey>): EncryptionKey? {
        if (keyRotation == null) return null

        val encryptionKey = keys.firstOrNull { it.rotation == keyRotation } ?: return null
        val decrypted = encryptionContextProvider.withEncryptionContext { decrypt(encryptionKey.key) }
        return EncryptionKey(decrypted)
    }

    private fun shareResponseToEntity(
        userAddress: UserAddress,
        shareResponse: ShareResponse,
        key: EncryptionKey?
    ): ShareEntity {
        val encryptedContent = if (shareResponse.content != null && key != null) {
            reencryptShareContents(shareResponse.content, key)
        } else {
            null
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
            encryptedContent = encryptedContent
        )
    }


    private fun shareEntityToShare(entity: ShareEntity): LoadingResult<Share> {
        val shareType = ShareType.map[entity.targetType]
        if (shareType == null) {
            val e = IllegalStateException("Unknown ShareType")
            PassLogger.w(TAG, e, "Unknown ShareType [shareType=${entity.targetType}]")
            return LoadingResult.Error(e)
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

        val share = Share(
            id = ShareId(entity.id),
            shareType = shareType,
            targetId = entity.targetId,
            permission = SharePermission(entity.permission),
            vaultId = VaultId(entity.vaultId),
            content = entity.encryptedContent.toOption(),
            expirationTime = entity.expirationTime?.let { Date(it) },
            createTime = Date(entity.createTime),
            color = color,
            icon = icon
        )
        return LoadingResult.Success(share)
    }

    private fun createVaultRequest(
        user: User,
        vault: NewVault,
        userAddress: UserAddress
    ): Pair<CreateVaultRequest, EncryptionKey> {
        val (name, description) = encryptionContextProvider.withEncryptionContext {
            decrypt(vault.name) to decrypt(vault.description)
        }

        val display = VaultV1.VaultDisplayPreferences.newBuilder()
            .setColor(vault.color.toProto())
            .setIcon(vault.icon.toProto())
            .build()
        val metadata = VaultV1.Vault.newBuilder()
            .setName(name)
            .setDescription(description)
            .setDisplay(display)
            .build()
        val (request, shareKey) = createVault.createVaultRequest(user, userAddress, metadata)
        return request.toRequest() to shareKey
    }

    internal data class ShareResponseEntity(
        val response: ShareResponse,
        val entity: ShareEntity
    )

    companion object {
        private const val TAG = "ShareRepositoryImpl"
    }
}

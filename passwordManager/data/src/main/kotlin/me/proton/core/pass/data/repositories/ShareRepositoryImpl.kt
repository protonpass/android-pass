package me.proton.core.pass.data.repositories

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.pass.data.crypto.CreateVault
import me.proton.core.pass.data.crypto.CreateVaultRequest
import me.proton.core.pass.data.crypto.OpenShare
import me.proton.core.pass.data.crypto.Utils
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.entities.ShareEntity
import me.proton.core.pass.data.local.LocalShareDataSource
import me.proton.core.pass.data.remote.RemoteShareDataSource
import me.proton.core.pass.data.responses.ShareResponse
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewVault
import me.proton.core.pass.domain.key.SigningKey
import me.proton.core.pass.domain.repositories.ShareRepository
import me.proton.core.pass.domain.repositories.VaultItemKeyList
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton_pass_vault_v1.VaultV1

class ShareRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val userAddressRepository: UserAddressRepository,
    private val remoteShareDataSource: RemoteShareDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val keyRepository: PublicAddressRepository,
    private val vaultKeyRepository: VaultKeyRepository,
    private val cryptoContext: CryptoContext,
    private val openShare: OpenShare,
    private val createVault: CreateVault,
) : ShareRepository {

    override suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault,
    ): Share {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val (request, _) = createVaultRequest(vault, userAddress)
        val shareResponse = remoteShareDataSource.createVault(userAddress.userId, request)

        // Replace the temporary rotationId we placed on the vaultKey with the actual rotationId
        val entity = database.inTransaction {
            val entity = shareResponseToEntity(userAddress, shareResponse)
            localShareDataSource.upsertShares(listOf(entity))

            val reencryptedEntity = reencryptShareEntityContents(userAddress, shareResponse, entity)
            localShareDataSource.upsertShares(listOf(reencryptedEntity))
            reencryptedEntity
        }

        val publicKeys = userAddress.keys.map { it.privateKey.publicKey(cryptoContext) }
        return shareEntityToShare(userAddress, publicKeys, entity)
    }

    override fun observeShares(userId: UserId): Flow<List<Share>> {
        return localShareDataSource.getAllSharesForUser(userId).mapLatest { shares ->
            val shareList = if (shares.isEmpty()) {
                performShareRefresh(userId)
            } else {
                shares
            }
            val userKeys = shareList
                .map { it.inviterEmail }
                .distinct()
                .associateWith { keyRepository.getPublicAddress(userId, it, source = Source.LocalIfAvailable) }
            shareList.map { entity ->
                val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
                val keys = requireNotNull(userKeys[entity.inviterEmail]?.keys?.map { it.publicKey })
                shareEntityToShare(userAddress, keys, entity)
            }
        }
    }

    override suspend fun refreshShares(userId: UserId) {
        performShareRefresh(userId)
    }

    override suspend fun getById(userId: UserId, shareId: ShareId): Share? {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

        // Check local
        var share = localShareDataSource.getById(userId, shareId)
        if (share == null) {
            // Check remote
            val shareResponse = remoteShareDataSource.getShareById(userId, shareId) ?: return null
            share = storeShares(userAddress, listOf(shareResponse))[0]
        }

        val addressKeys = keyRepository.getPublicAddress(userId, share.inviterEmail, source = Source.LocalIfAvailable)
        return shareEntityToShare(userAddress, addressKeys.keys.publicKeyRing().keys, share)
    }

    private suspend fun performShareRefresh(userId: UserId): List<ShareEntity> {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        val shares = remoteShareDataSource.getShares(userAddress.userId)
        return storeShares(userAddress, shares)
    }

    private suspend fun storeShares(userAddress: UserAddress, shares: List<ShareResponse>): List<ShareEntity> {
        // The ShareEntity will still not contain the reencrypted contents and signing key passphrase,
        // because for doing so we need the vault keys, and as the share has not yet been persisted to
        // the database, the vaultKey fetching would store them into the database and the shareId FK
        // would fail, so we first store the share without the reencryption, fetch the vaultKeys, and
        // then we reencrypt the data
        val entities = shares.map {
            ShareResponseEntity(it, shareResponseToEntity(userAddress, it))
        }

        return database.inTransaction {
            localShareDataSource.upsertShares(entities.map { it.entity })

            // We have now inserted the shares without the reencrypted content
            // Now we fetch the vaultKeys for each share, reencrypt the contents and prepare the entities
            // with reencrypted contents
            val updatedEntities = entities.map {
                reencryptShareEntityContents(userAddress, it.response, it.entity)
            }

            // Persist the updates into the database
            localShareDataSource.upsertShares(updatedEntities)
            updatedEntities
        }
    }

    private suspend fun reencryptShareEntityContents(
        userAddress: UserAddress,
        response: ShareResponse,
        entity: ShareEntity
    ): ShareEntity {
        val signingKey = SigningKey(Utils.readKey(response.signingKey, isPrimary = true))
        val vaultKeys = vaultKeyRepository.getVaultKeys(userAddress, ShareId(response.shareId), signingKey)
        return entity.copy(
            keystoreEncryptedContent = openShare.reencryptContent(response, vaultKeys),
            keystoreEncryptedPassphrase = openShare.reencryptSigningKeyPassphrase(response.signingKeyPassphrase, userAddress)
        )
    }

    private suspend fun shareResponseToEntity(
        userAddress: UserAddress,
        shareResponse: ShareResponse,
    ): ShareEntity {
        val inviterKeys = keyRepository.getPublicAddress(userAddress.userId, shareResponse.inviterEmail, source = Source.LocalIfAvailable)
        val inviterPublicKeys = inviterKeys.keys.publicKeyRing().keys
        return openShare.responseToEntity(
            shareResponse,
            userAddress,
            inviterPublicKeys,
        )
    }

    private suspend fun shareEntityToShare(
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>,
        entity: ShareEntity,
    ): Share {
        val signingKey = SigningKey(Utils.readKey(entity.signingKey, isPrimary = true))
        val vaultKeys = vaultKeyRepository.getVaultKeys(userAddress, ShareId(entity.id), signingKey)

        return openShare.open(entity, userAddress, inviterKeys, vaultKeys)
    }

    private fun createVaultRequest(
        vault: NewVault,
        userAddress: UserAddress
    ): Pair<CreateVaultRequest, VaultItemKeyList> {
        val metadata = VaultV1.Vault.newBuilder()
            .setName(vault.name.decrypt(cryptoContext.keyStoreCrypto))
            .setDescription(vault.description.decrypt(cryptoContext.keyStoreCrypto))
            .build()
        return createVault.createVaultRequest(metadata, userAddress)
    }

    internal data class ShareResponseEntity(
        val response: ShareResponse,
        val entity: ShareEntity,
    )
}

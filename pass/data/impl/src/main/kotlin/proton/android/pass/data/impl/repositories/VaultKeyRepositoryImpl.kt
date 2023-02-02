package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.crypto.api.usecases.OpenKeys
import proton.android.pass.data.api.repositories.VaultItemKeyList
import proton.android.pass.data.api.repositories.VaultKeyRepository
import proton.android.pass.data.impl.db.entities.ItemKeyEntity
import proton.android.pass.data.impl.db.entities.VaultKeyEntity
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.extensions.toVaultItemKeyList
import proton.android.pass.data.impl.local.LocalVaultItemKeyDataSource
import proton.android.pass.data.impl.local.VaultItemKeyEntityList
import proton.android.pass.data.impl.remote.RemoteVaultItemKeyDataSource
import proton.android.pass.data.impl.remote.VaultItemKeyResponseList
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.pass.domain.ShareId
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey
import javax.inject.Inject

class VaultKeyRepositoryImpl @Inject constructor(
    private val localDataSource: LocalVaultItemKeyDataSource,
    private val remoteDataSource: RemoteVaultItemKeyDataSource,
    private val openKeys: OpenKeys
) : VaultKeyRepository {

    override suspend fun getVaultKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean
    ): LoadingResult<List<VaultKey>> = withContext(Dispatchers.IO) {
        val result = getVaultItemKeys(
            userAddress,
            shareId,
            signingKey,
            forceRefresh,
            shouldStoreLocally
        )
        return@withContext when (result) {
            is LoadingResult.Error -> LoadingResult.Error(result.exception)
            LoadingResult.Loading -> LoadingResult.Loading
            is LoadingResult.Success -> LoadingResult.Success(result.data.vaultKeyList)
        }
    }

    override suspend fun getVaultKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): LoadingResult<VaultKey> = withContext(Dispatchers.IO) {
        val key = localDataSource.getVaultKeyById(userAddress, shareId, keyId)
        if (key != null) {
            return@withContext LoadingResult.Success(vaultKeyEntityToDomain(key, key.rotationId))
        }

        return@withContext getVaultItemKeys(userAddress, shareId, signingKey, true)
            .map { keys -> requireNotNull(keys.vaultKeyList.firstOrNull { it.rotationId == keyId }) }
    }

    override suspend fun getItemKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): LoadingResult<ItemKey> = withContext(Dispatchers.IO) {
        val key = localDataSource.getItemKeyById(userAddress, shareId, keyId)
        if (key != null) {
            return@withContext LoadingResult.Success(itemKeyEntityToDomain(key, key.rotationId))
        }

        // We didn't find it on the local storage
        return@withContext getVaultItemKeys(userAddress, shareId, signingKey, true)
            .map { keys -> requireNotNull(keys.itemKeyList.firstOrNull { it.rotationId == keyId }) }
    }

    override suspend fun getLatestVaultKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): LoadingResult<VaultKey> = withContext(Dispatchers.IO) {
        getVaultKeys(userAddress, shareId, signingKey, forceRefresh)
            .map { keys -> requireNotNull(keys.maxByOrNull { it.rotation }) }
    }

    override suspend fun getLatestVaultItemKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): LoadingResult<Pair<VaultKey, ItemKey>> = withContext(Dispatchers.IO) {
        getVaultItemKeys(userAddress, shareId, signingKey, forceRefresh)
            .map { keys ->
                val latestVaultKey = requireNotNull(keys.vaultKeyList.maxByOrNull { it.rotation })
                val latestItemKey = requireNotNull(
                    keys.itemKeyList.firstOrNull { it.rotationId == latestVaultKey.rotationId }
                )
                Pair(latestVaultKey, latestItemKey)
            }
    }

    private suspend fun getVaultItemKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean = true
    ): LoadingResult<VaultItemKeyList> {
        if (!forceRefresh) {
            val keys = localDataSource.getKeys(userAddress, shareId)
            if (keys.vaultKeys.isNotEmpty()) {
                return LoadingResult.Success(entityToDomain(keys))
            }
        }

        return when (val result = remoteDataSource.getKeys(userAddress.userId, shareId)) {
            is LoadingResult.Success -> {
                val open = openKeys.open(result.data.toCrypto(), signingKey, userAddress)

                val vaultKeyPassphrases = open.vaultKeyList
                    .associate { it.rotationId to it.encryptedKeyPassphrase }
                val itemKeyPassphrases = open.itemKeyList
                    .associate { it.rotationId to it.encryptedKeyPassphrase }

                if (shouldStoreLocally) {
                    val entityKeys = domainToEntity(
                        result.data,
                        userAddress,
                        shareId,
                        vaultKeyPassphrases,
                        itemKeyPassphrases
                    )
                    localDataSource.storeKeys(userAddress, shareId, entityKeys)
                }
                LoadingResult.Success(open.toVaultItemKeyList())
            }
            is LoadingResult.Error -> LoadingResult.Error(result.exception)
            LoadingResult.Loading -> LoadingResult.Loading
        }
    }

    private fun domainToEntity(
        list: VaultItemKeyResponseList,
        userAddress: UserAddress,
        shareId: ShareId,
        encryptedVaultKeyPassphrases: Map<String, EncryptedByteArray?>,
        encryptedItemKeyPassphrases: Map<String, EncryptedByteArray?>
    ): VaultItemKeyEntityList {
        val vaultKeys = list.vaultKeys.map {
            VaultKeyEntity(
                rotationId = it.rotationId,
                userId = userAddress.userId.id,
                addressId = userAddress.addressId.id,
                shareId = shareId.id,
                rotation = it.rotation,
                key = it.key,
                keyPassphrase = it.keyPassphrase,
                keySignature = it.keySignature,
                createTime = it.createTime,
                encryptedKeyPassphrase = encryptedVaultKeyPassphrases[it.rotationId]
            )
        }
        val itemKeys = list.itemKeys.map {
            ItemKeyEntity(
                rotationId = it.rotationId,
                userId = userAddress.userId.id,
                addressId = userAddress.addressId.id,
                shareId = shareId.id,
                key = it.key,
                keyPassphrase = it.keyPassphrase,
                keySignature = it.keySignature,
                createTime = it.createTime,
                encryptedKeyPassphrase = encryptedItemKeyPassphrases[it.rotationId]
            )
        }

        return VaultItemKeyEntityList(vaultKeys, itemKeys)
    }

    private fun entityToDomain(list: VaultItemKeyEntityList): VaultItemKeyList {
        val maxRotationId = list.vaultKeys
            .maxByOrNull { it.rotation }
            ?.rotationId
            ?: return VaultItemKeyList(emptyList(), emptyList())

        val vaultKeys = list.vaultKeys.map { vaultKeyEntityToDomain(it, maxRotationId) }
        val itemKeys = list.itemKeys.map { itemKeyEntityToDomain(it, maxRotationId) }

        return VaultItemKeyList(vaultKeys, itemKeys)
    }

    private fun vaultKeyEntityToDomain(entity: VaultKeyEntity, maxRotationId: String): VaultKey {
        val key = if (entity.encryptedKeyPassphrase == null) {
            ArmoredKey.Public(
                entity.key,
                PublicKey(
                    key = entity.key,
                    isPrimary = entity.rotationId == maxRotationId,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true
                )
            )
        } else {
            ArmoredKey.Private(
                entity.key,
                PrivateKey(
                    key = entity.key,
                    isPrimary = entity.rotationId == maxRotationId,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true,
                    passphrase = entity.encryptedKeyPassphrase
                )
            )
        }
        return VaultKey(
            rotationId = entity.rotationId,
            rotation = entity.rotation,
            key = key,
            encryptedKeyPassphrase = entity.encryptedKeyPassphrase
        )
    }

    private fun itemKeyEntityToDomain(entity: ItemKeyEntity, maxRotationId: String): ItemKey {
        val key = if (entity.encryptedKeyPassphrase == null) {
            ArmoredKey.Public(
                entity.key,
                PublicKey(
                    key = entity.key,
                    isPrimary = entity.rotationId == maxRotationId,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true
                )
            )
        } else {
            ArmoredKey.Private(
                entity.key,
                PrivateKey(
                    key = entity.key,
                    isPrimary = entity.rotationId == maxRotationId,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true,
                    passphrase = entity.encryptedKeyPassphrase
                )
            )
        }
        return ItemKey(
            rotationId = entity.rotationId,
            key = key,
            encryptedKeyPassphrase = entity.encryptedKeyPassphrase
        )
    }
}

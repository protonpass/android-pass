package me.proton.core.pass.data.repositories

import javax.inject.Inject
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.pass.data.crypto.OpenKeys
import me.proton.core.pass.data.db.entities.ItemKeyEntity
import me.proton.core.pass.data.db.entities.VaultKeyEntity
import me.proton.core.pass.data.local.LocalVaultItemKeyDataSource
import me.proton.core.pass.data.local.VaultItemKeyEntityList
import me.proton.core.pass.data.remote.RemoteVaultItemKeyDataSource
import me.proton.core.pass.data.remote.VaultItemKeyResponseList
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.SigningKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.repositories.VaultItemKeyList
import me.proton.core.pass.domain.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress

class VaultKeyRepositoryImpl @Inject constructor(
    private val localDataSource: LocalVaultItemKeyDataSource,
    private val remoteDataSource: RemoteVaultItemKeyDataSource,
    private val openKeys: OpenKeys
) : VaultKeyRepository {

    override suspend fun getVaultKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): List<VaultKey> =
        getVaultItemKeys(userAddress, shareId, signingKey, forceRefresh).vaultKeyList

    override suspend fun getVaultKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): VaultKey {
        val key = localDataSource.getVaultKeyById(userAddress, shareId, keyId)
        if (key != null) {
            return vaultKeyEntityToDomain(key, key.rotationId)
        }

        val keys = getVaultItemKeys(userAddress, shareId, signingKey, true)
        return requireNotNull(keys.vaultKeyList.firstOrNull { it.rotationId == keyId })
    }

    override suspend fun getItemKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): ItemKey {
        val key = localDataSource.getItemKeyById(userAddress, shareId, keyId)
        if (key != null) {
            return itemKeyEntityToDomain(key, key.rotationId)
        }

        // We didn't find it on the local storage
        val keys = getVaultItemKeys(userAddress, shareId, signingKey, true)
        return requireNotNull(keys.itemKeyList.firstOrNull { it.rotationId == keyId })
    }

    override suspend fun getLatestVaultKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): VaultKey {
        val keys = getVaultKeys(userAddress, shareId, signingKey, forceRefresh)
        return requireNotNull(keys.maxByOrNull { it.rotation })
    }

    override suspend fun getLatestVaultItemKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): Pair<VaultKey, ItemKey> {
        val keys = getVaultItemKeys(userAddress, shareId, signingKey, forceRefresh)
        val latestVaultKey = requireNotNull(keys.vaultKeyList.maxByOrNull { it.rotation })
        val latestItemKey = requireNotNull(
            keys.itemKeyList.firstOrNull { it.rotationId == latestVaultKey.rotationId }
        )
        return Pair(latestVaultKey, latestItemKey)
    }

    override suspend fun storeVaultKey(
        userAddress: UserAddress,
        shareId: ShareId,
        vaultKey: VaultKey,
        keyPassphrase: String?,
        keySignature: String,
        createTime: Long
    ) {
        val entity = VaultKeyEntity(
            rotationId = vaultKey.rotationId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            shareId = shareId.id,
            rotation = vaultKey.rotation,
            key = vaultKey.key.armored,
            keyPassphrase = keyPassphrase,
            keySignature = keySignature,
            createTime = createTime,
            encryptedKeyPassphrase = vaultKey.encryptedKeyPassphrase
        )
        localDataSource.storeKeys(
            userAddress, shareId,
            VaultItemKeyEntityList(
                vaultKeys = listOf(entity),
                itemKeys = emptyList()
            )
        )
    }

    override suspend fun storeItemKey(
        userAddress: UserAddress,
        shareId: ShareId,
        itemKey: ItemKey,
        keyPassphrase: String?,
        keySignature: String,
        createTime: Long
    ) {
        val entity = ItemKeyEntity(
            rotationId = itemKey.rotationId,
            userId = userAddress.userId.id,
            addressId = userAddress.addressId.id,
            shareId = shareId.id,
            key = itemKey.key.armored,
            keyPassphrase = keyPassphrase,
            keySignature = keySignature,
            createTime = createTime,
            encryptedKeyPassphrase = itemKey.encryptedKeyPassphrase
        )
        localDataSource.storeKeys(
            userAddress, shareId,
            VaultItemKeyEntityList(
                vaultKeys = emptyList(),
                itemKeys = listOf(entity)
            )
        )
    }

    private suspend fun getVaultItemKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): VaultItemKeyList {
        if (!forceRefresh) {
            val keys = localDataSource.getKeys(userAddress, shareId)
            if (keys.vaultKeys.isNotEmpty()) {
                return entityToDomain(keys)
            }
        }

        val remoteKeys = remoteDataSource.getKeys(userAddress.userId, shareId)
        val open = openKeys.openKeys(remoteKeys, signingKey, userAddress)

        val vaultKeyPassphrases = open.vaultKeyList
            .associate { it.rotationId to it.encryptedKeyPassphrase }
        val itemKeyPassphrases = open.itemKeyList
            .associate { it.rotationId to it.encryptedKeyPassphrase }

        val entityKeys = domainToEntity(
            remoteKeys,
            userAddress,
            shareId,
            vaultKeyPassphrases,
            itemKeyPassphrases
        )
        localDataSource.storeKeys(userAddress, shareId, entityKeys)
        return open
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
            .maxByOrNull { it.rotation }?.rotationId
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

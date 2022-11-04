package me.proton.pass.data.local

import javax.inject.Inject
import me.proton.pass.data.db.PassDatabase
import me.proton.pass.data.db.entities.ItemKeyEntity
import me.proton.pass.data.db.entities.VaultKeyEntity
import me.proton.pass.domain.ShareId
import me.proton.core.user.domain.entity.UserAddress

class LocalVaultItemKeyDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalVaultItemKeyDataSource {
    override suspend fun getKeys(userAddress: UserAddress, shareId: ShareId): VaultItemKeyEntityList {
        val vaultKeys = database.vaultKeysDao().getAllForShare(userAddress.userId.id, shareId.id)
        val itemKeys = database.itemKeysDao().getAllForShare(userAddress.userId.id, shareId.id)
        return VaultItemKeyEntityList(vaultKeys, itemKeys)
    }

    override suspend fun storeKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        keys: VaultItemKeyEntityList
    ) {
        database.vaultKeysDao().insertOrUpdate(*keys.vaultKeys.toTypedArray())
        database.itemKeysDao().insertOrUpdate(*keys.itemKeys.toTypedArray())
    }

    override suspend fun getVaultKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        rotationId: String
    ): VaultKeyEntity? =
        database.vaultKeysDao().getById(userAddress.userId.id, shareId.id, rotationId)

    override suspend fun getItemKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        rotationId: String
    ): ItemKeyEntity? =
        database.itemKeysDao().getById(userAddress.userId.id, shareId.id, rotationId)
}

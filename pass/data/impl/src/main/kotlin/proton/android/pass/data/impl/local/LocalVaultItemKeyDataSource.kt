package proton.android.pass.data.impl.local

import proton.android.pass.data.impl.db.entities.ItemKeyEntity
import proton.android.pass.data.impl.db.entities.VaultKeyEntity
import proton.pass.domain.ShareId
import me.proton.core.user.domain.entity.UserAddress

data class VaultItemKeyEntityList(
    val vaultKeys: List<VaultKeyEntity>,
    val itemKeys: List<ItemKeyEntity>
)

interface LocalVaultItemKeyDataSource {
    suspend fun getKeys(userAddress: UserAddress, shareId: ShareId): VaultItemKeyEntityList
    suspend fun getVaultKeyById(userAddress: UserAddress, shareId: ShareId, rotationId: String): VaultKeyEntity?
    suspend fun getItemKeyById(userAddress: UserAddress, shareId: ShareId, rotationId: String): ItemKeyEntity?
    suspend fun storeKeys(userAddress: UserAddress, shareId: ShareId, keys: VaultItemKeyEntityList)
}

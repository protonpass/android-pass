package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.responses.ItemKeyData
import me.proton.core.pass.data.responses.VaultKeyData
import me.proton.core.pass.domain.ShareId

data class VaultItemKeyResponseList(
    val vaultKeys: List<VaultKeyData>,
    val itemKeys: List<ItemKeyData>
)

interface RemoteVaultItemKeyDataSource {
    suspend fun getKeys(userId: UserId, shareId: ShareId): VaultItemKeyResponseList
}

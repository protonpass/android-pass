package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.data.responses.ItemKeyData
import me.proton.pass.data.responses.VaultKeyData
import me.proton.pass.domain.ShareId

data class VaultItemKeyResponseList(
    val vaultKeys: List<VaultKeyData>,
    val itemKeys: List<ItemKeyData>
)

interface RemoteVaultItemKeyDataSource {
    suspend fun getKeys(userId: UserId, shareId: ShareId): Result<VaultItemKeyResponseList>
}

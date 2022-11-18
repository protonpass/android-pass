package me.proton.android.pass.data.impl.remote

import me.proton.android.pass.data.impl.responses.ItemKeyData
import me.proton.android.pass.data.impl.responses.VaultKeyData
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ShareId

data class VaultItemKeyResponseList(
    val vaultKeys: List<VaultKeyData>,
    val itemKeys: List<ItemKeyData>
)

interface RemoteVaultItemKeyDataSource {
    suspend fun getKeys(userId: UserId, shareId: ShareId): Result<VaultItemKeyResponseList>
}

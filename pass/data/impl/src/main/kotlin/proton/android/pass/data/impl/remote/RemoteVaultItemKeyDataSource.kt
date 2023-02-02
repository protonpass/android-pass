package proton.android.pass.data.impl.remote

import proton.android.pass.data.impl.responses.ItemKeyData
import proton.android.pass.data.impl.responses.VaultKeyData
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ShareId

data class VaultItemKeyResponseList(
    val vaultKeys: List<VaultKeyData>,
    val itemKeys: List<ItemKeyData>
)

interface RemoteVaultItemKeyDataSource {
    suspend fun getKeys(userId: UserId, shareId: ShareId): LoadingResult<VaultItemKeyResponseList>
}

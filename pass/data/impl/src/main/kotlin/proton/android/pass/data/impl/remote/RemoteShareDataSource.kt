package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.ShareId

interface RemoteShareDataSource {
    suspend fun createVault(userId: UserId, body: CreateVaultRequest): Result<ShareResponse>
    suspend fun deleteVault(userId: UserId, shareId: ShareId): Result<Unit>
    suspend fun getShares(userId: UserId): Result<List<ShareResponse>>
    suspend fun getShareById(userId: UserId, shareId: ShareId): Result<ShareResponse?>
}

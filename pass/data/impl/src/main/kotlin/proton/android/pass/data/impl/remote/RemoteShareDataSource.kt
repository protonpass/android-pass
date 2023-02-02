package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.ShareId

interface RemoteShareDataSource {
    suspend fun createVault(userId: UserId, body: CreateVaultRequest): LoadingResult<ShareResponse>
    suspend fun deleteVault(userId: UserId, shareId: ShareId): LoadingResult<Unit>
    suspend fun getShares(userId: UserId): LoadingResult<List<ShareResponse>>
    suspend fun getShareById(userId: UserId, shareId: ShareId): LoadingResult<ShareResponse?>
}

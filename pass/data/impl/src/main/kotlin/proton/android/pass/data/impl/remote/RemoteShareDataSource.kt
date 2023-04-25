package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.ShareId

interface RemoteShareDataSource {
    suspend fun createVault(userId: UserId, body: CreateVaultRequest): ShareResponse
    suspend fun updateVault(userId: UserId, shareId: ShareId, body: UpdateVaultRequest): ShareResponse
    suspend fun deleteVault(userId: UserId, shareId: ShareId)
    suspend fun getShares(userId: UserId): List<ShareResponse>
    suspend fun fetchShareById(userId: UserId, shareId: ShareId): ShareResponse?
    suspend fun markAsPrimary(userId: UserId, shareId: ShareId)
}

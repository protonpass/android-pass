package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.data.requests.CreateVaultRequest
import me.proton.core.pass.data.responses.ShareResponse
import me.proton.core.pass.domain.ShareId

interface RemoteShareDataSource {
    suspend fun createVault(userId: UserId, body: CreateVaultRequest): Result<ShareResponse>
    suspend fun getShares(userId: UserId): Result<List<ShareResponse>>
    suspend fun getShareById(userId: UserId, shareId: ShareId): Result<ShareResponse?>
}

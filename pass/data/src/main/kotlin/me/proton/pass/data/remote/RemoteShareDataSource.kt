package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.data.requests.CreateVaultRequest
import me.proton.pass.data.responses.ShareResponse
import me.proton.pass.domain.ShareId

interface RemoteShareDataSource {
    suspend fun createVault(userId: UserId, body: CreateVaultRequest): Result<ShareResponse>
    suspend fun getShares(userId: UserId): Result<List<ShareResponse>>
    suspend fun getShareById(userId: UserId, shareId: ShareId): Result<ShareResponse?>
}

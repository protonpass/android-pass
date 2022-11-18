package me.proton.android.pass.data.impl.remote

import me.proton.android.pass.data.impl.requests.CreateVaultRequest
import me.proton.android.pass.data.impl.responses.ShareResponse
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ShareId

interface RemoteShareDataSource {
    suspend fun createVault(userId: UserId, body: CreateVaultRequest): Result<ShareResponse>
    suspend fun getShares(userId: UserId): Result<List<ShareResponse>>
    suspend fun getShareById(userId: UserId, shareId: ShareId): Result<ShareResponse?>
}

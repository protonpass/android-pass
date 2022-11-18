package me.proton.android.pass.data.impl.remote

import me.proton.android.pass.data.impl.requests.CreateVaultRequest
import me.proton.android.pass.data.impl.responses.ShareResponse
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.common.api.toResult
import me.proton.pass.data.api.PasswordManagerApi
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteShareDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteShareDataSource {
    override suspend fun createVault(
        userId: UserId,
        body: CreateVaultRequest
    ): Result<ShareResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { createVault(body) }
            .toResult()
            .map { it.share }

    override suspend fun getShares(userId: UserId): Result<List<ShareResponse>> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                val shares = getShares()
                val shareList = shares.shares.map { getShare(it.shareId) }
                shareList.map { it.share }
            }
            .toResult()

    override suspend fun getShareById(userId: UserId, shareId: ShareId): Result<ShareResponse?> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                val res = getShare(shareId.id)
                if (res.code == PROTON_RESPONSE_OK) {
                    res.share
                } else {
                    null
                }
            }
            .toResult()

    companion object {
        private const val PROTON_RESPONSE_OK = 1000
    }
}

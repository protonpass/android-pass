package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.map
import me.proton.core.pass.common.api.toResult
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.requests.CreateVaultRequest
import me.proton.core.pass.data.responses.ShareResponse
import me.proton.core.pass.domain.ShareId
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

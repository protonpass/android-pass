package me.proton.core.pass.data.remote

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.crypto.CreateVaultRequest
import me.proton.core.pass.data.responses.ShareResponse
import me.proton.core.pass.domain.ShareId

class RemoteShareDataSourceImpl @Inject constructor(
    private val api: ApiProvider,
) : RemoteShareDataSource {
    override suspend fun createVault(
        userId: UserId,
        body: CreateVaultRequest
    ): ShareResponse =
        api.get<PasswordManagerApi>(userId).invoke {
            createVault(body)
        }.valueOrThrow.share

    override suspend fun getShares(userId: UserId): List<ShareResponse> =
        api.get<PasswordManagerApi>(userId).invoke {
            val shares = getShares()
            val shareList = shares.shares.map { getShare(it.shareId) }
            shareList.map { it.share }
        }.valueOrThrow

    override suspend fun getShareById(userId: UserId, shareId: ShareId): ShareResponse? =
        api.get<PasswordManagerApi>(userId).invoke {
            val res = getShare(shareId.id)
            if (res.code == 1000) {
                res.share
            } else {
                null
            }
        }.valueOrThrow
}

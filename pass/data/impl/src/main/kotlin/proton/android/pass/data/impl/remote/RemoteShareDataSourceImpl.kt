package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toLoadingResult
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteShareDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteShareDataSource {
    override suspend fun createVault(
        userId: UserId,
        body: CreateVaultRequest
    ): LoadingResult<ShareResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke { createVault(body) }
            .toLoadingResult()
            .map { it.share }

    override suspend fun deleteVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        api.get<PasswordManagerApi>(userId)
            .invoke { deleteVault(shareId.id) }
            .toLoadingResult()
            .map { }

    override suspend fun getShares(userId: UserId): LoadingResult<List<ShareResponse>> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                getShares().shares
            }
            .toLoadingResult()

    override suspend fun getShareById(userId: UserId, shareId: ShareId): LoadingResult<ShareResponse?> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                val res = getShare(shareId.id)
                if (res.code == PROTON_RESPONSE_OK) {
                    res.share
                } else {
                    null
                }
            }
            .toLoadingResult()

    companion object {
        private const val PROTON_RESPONSE_OK = 1000
    }
}

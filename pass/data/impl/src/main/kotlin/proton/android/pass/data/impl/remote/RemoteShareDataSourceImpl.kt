package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toLoadingResult
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteShareDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteShareDataSource {
    override suspend fun createVault(
        userId: UserId,
        body: CreateVaultRequest
    ): LoadingResult<ShareResponse> {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { createVault(body) }
        when (res) {
            is ApiResult.Success -> return LoadingResult.Success(res.value.share)
            is ApiResult.Error -> {
                if (res is ApiResult.Error.Http) {
                    if (res.proton?.code == CODE_CANNOT_CREATE_MORE_VAULTS) {
                        return LoadingResult.Error(CannotCreateMoreVaultsError())
                    }
                }
                return LoadingResult.Error(res.cause ?: Exception("Create vault failed"))
            }
        }
    }

    override suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        body: UpdateVaultRequest
    ): ShareResponse =
        api.get<PasswordManagerApi>(userId)
            .invoke { updateVault(shareId.id, body).share }
            .valueOrThrow


    override suspend fun deleteVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        api.get<PasswordManagerApi>(userId)
            .invoke { deleteVault(shareId.id) }
            .toLoadingResult()
            .map { }

    override suspend fun getShares(userId: UserId): List<ShareResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                getShares().shares
            }
            .valueOrThrow

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

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId) =
        api.get<PasswordManagerApi>(userId)
            .invoke { markAsPrimary(shareId.id) }
            .valueOrThrow

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val PROTON_RESPONSE_OK = 1000
        private const val CODE_CANNOT_CREATE_MORE_VAULTS = 300007
    }
}

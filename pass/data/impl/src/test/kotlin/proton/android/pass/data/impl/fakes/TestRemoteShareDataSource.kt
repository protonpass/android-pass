package proton.android.pass.data.impl.fakes

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.pass.domain.ShareId

class TestRemoteShareDataSource : RemoteShareDataSource {

    private var createVaultResponse: LoadingResult<ShareResponse> =
        LoadingResult.Error(IllegalStateException("createVaultResponse not set"))
    private var updateVaultResponse: Result<ShareResponse> =
        Result.failure(IllegalStateException("updateVaultResponse not set"))
    private var deleteVaultResponse: LoadingResult<Unit> =
        LoadingResult.Error(IllegalStateException("deleteVaultResponse not set"))
    private var getSharesResponse: Result<List<ShareResponse>> =
        Result.failure(IllegalStateException("getSharesResponse not set"))
    private var getShareByIdResponse: LoadingResult<ShareResponse?> =
        LoadingResult.Error(IllegalStateException("getShareByIdResponse not set"))
    private var markAsPrimaryResponse: Result<Unit> =
        Result.failure(IllegalStateException("markAsPrimaryResponse not set"))

    fun setCreateVaultResponse(value: LoadingResult<ShareResponse>) {
        createVaultResponse = value
    }

    fun setUpdateVaultResponse(value: Result<ShareResponse>) {
        updateVaultResponse = value
    }

    fun setDeleteVaultResponse(value: LoadingResult<Unit>) {
        deleteVaultResponse = value
    }

    fun setGetSharesResponse(value: Result<List<ShareResponse>>) {
        getSharesResponse = value
    }

    fun setGetShareByIdResponse(value: LoadingResult<ShareResponse?>) {
        getShareByIdResponse = value
    }

    fun setMarkAsPrimaryResponse(value: Result<Unit>) {
        markAsPrimaryResponse = value
    }

    override suspend fun createVault(
        userId: UserId,
        body: CreateVaultRequest
    ): LoadingResult<ShareResponse> = createVaultResponse

    override suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        body: UpdateVaultRequest
    ): ShareResponse = updateVaultResponse.getOrThrow()

    override suspend fun deleteVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        deleteVaultResponse

    override suspend fun getShares(userId: UserId): List<ShareResponse> =
        getSharesResponse.getOrThrow()

    override suspend fun fetchShareById(
        userId: UserId,
        shareId: ShareId
    ): LoadingResult<ShareResponse?> = getShareByIdResponse

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId) {
        markAsPrimaryResponse.getOrThrow()
    }
}

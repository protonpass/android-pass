package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault

class TestShareRepository : ShareRepository {

    private var createVaultResult: Result<Share> =
        Result.failure(IllegalStateException("CreateVaultResult not set"))
    private var refreshSharesResult: RefreshSharesResult =
        RefreshSharesResult(emptySet(), emptySet())
    private var observeSharesFlow = testFlow<Result<List<Share>>>()
    private var deleteVault: Result<Unit> =
        Result.failure(IllegalStateException("DeleteVaultResult not set"))
    private var getByIdResult: Result<Share> =
        Result.failure(IllegalStateException("GetByIdResult not set"))

    private var updateVaultResult: Result<Share> =
        Result.failure(IllegalStateException("UpdateVaultResult not set"))

    private var markAsPrimaryResult: Result<Share> =
        Result.failure(IllegalStateException("MarkAsPrimaryResult not set"))

    private var deleteSharesResult: Result<Unit> = Result.success(Unit)

    private val deleteVaultMemory: MutableList<ShareId> = mutableListOf()
    fun deleteVaultMemory(): List<ShareId> = deleteVaultMemory

    fun setCreateVaultResult(result: Result<Share>) {
        createVaultResult = result
    }

    fun setRefreshSharesResult(result: RefreshSharesResult) {
        refreshSharesResult = result
    }

    fun setDeleteVaultResult(result: Result<Unit>) {
        deleteVault = result
    }

    fun setGetByIdResult(result: Result<Share>) {
        getByIdResult = result
    }

    fun emitObserveShares(value: Result<List<Share>>) {
        observeSharesFlow.tryEmit(value)
    }

    fun setUpdateVaultResult(value: Result<Share>) {
        updateVaultResult = value
    }

    fun setMarkAsPrimaryResult(value: Result<Share>) {
        markAsPrimaryResult = value
    }

    fun setDeleteSharesResult(value: Result<Unit>) {
        deleteSharesResult = value
    }

    override suspend fun createVault(userId: SessionUserId, vault: NewVault): Share =
        createVaultResult.getOrThrow()

    override suspend fun deleteVault(userId: UserId, shareId: ShareId) {
        deleteVaultMemory.add(shareId)
        deleteVault.getOrThrow()
    }

    override suspend fun refreshShares(userId: UserId): RefreshSharesResult =
        refreshSharesResult

    override fun observeAllShares(userId: SessionUserId): Flow<List<Share>> =
        observeSharesFlow.map { it.getOrThrow() }

    override suspend fun getById(userId: UserId, shareId: ShareId): Share =
        getByIdResult.getOrThrow()

    override suspend fun updateVault(userId: UserId, shareId: ShareId, vault: NewVault): Share =
        updateVaultResult.getOrThrow()

    override suspend fun markAsPrimary(userId: UserId, shareId: ShareId): Share =
        markAsPrimaryResult.getOrThrow()

    override suspend fun deleteSharesForUser(userId: UserId) = deleteSharesResult.getOrThrow()
}

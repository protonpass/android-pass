package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.repositories.ShareRepository
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault

class TestShareRepository : ShareRepository {

    private var createVaultResult: Result<Share> = Result.Loading
    private var refreshSharesResult: Result<List<Share>> = Result.Loading
    private var observeSharesFlow = MutableSharedFlow<Result<List<Share>>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )
    private var getByIdResult: Result<Share?> = Result.Loading

    fun setCreateVaultResult(result: Result<Share>) {
        createVaultResult = result
    }

    fun setRefreshSharesResult(result: Result<List<Share>>) {
        refreshSharesResult = result
    }

    fun setGetByIdResult(result: Result<Share?>) {
        getByIdResult = result
    }

    fun emitObserveShares(value: Result<List<Share>>) {
        observeSharesFlow.tryEmit(value)
    }

    override suspend fun createVault(userId: SessionUserId, vault: NewVault): Result<Share> =
        createVaultResult

    override suspend fun deleteVault(userId: UserId, shareId: ShareId): Result<Unit> =
        Result.Success(Unit)

    override suspend fun refreshShares(userId: UserId): Result<List<Share>> =
        refreshSharesResult

    override fun observeShares(userId: SessionUserId): Flow<Result<List<Share>>> =
        observeSharesFlow

    override suspend fun getById(userId: UserId, shareId: ShareId): Result<Share?> =
        getByIdResult
}

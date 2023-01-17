package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault

interface ShareRepository {
    suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): Result<Share>

    suspend fun deleteVault(
        userId: UserId,
        shareId: ShareId
    ): Result<Unit>

    suspend fun selectVault(
        userId: UserId,
        shareId: ShareId
    ): Result<Unit>

    suspend fun refreshShares(userId: UserId): Result<List<Share>>

    fun observeAllShares(userId: SessionUserId): Flow<Result<List<Share>>>

    fun observeSelectedShares(userId: SessionUserId): Flow<Result<List<Share>>>

    suspend fun getById(userId: UserId, shareId: ShareId): Result<Share?>
}

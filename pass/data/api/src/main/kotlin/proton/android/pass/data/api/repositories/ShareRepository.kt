package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault

interface ShareRepository {
    suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): LoadingResult<Share>

    suspend fun deleteVault(
        userId: UserId,
        shareId: ShareId
    ): LoadingResult<Unit>

    suspend fun selectVault(
        userId: UserId,
        shareId: ShareId
    ): LoadingResult<Unit>

    suspend fun refreshShares(userId: UserId): LoadingResult<List<Share>>

    fun observeAllShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>>

    fun observeSelectedShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>>

    suspend fun getById(userId: UserId, shareId: ShareId): LoadingResult<Share?>

    suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        vault: NewVault
    ): Share
}

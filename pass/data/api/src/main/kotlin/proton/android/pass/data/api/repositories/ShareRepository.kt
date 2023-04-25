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
    ): Share

    suspend fun deleteVault(
        userId: UserId,
        shareId: ShareId
    ): LoadingResult<Unit>

    suspend fun refreshShares(userId: UserId): RefreshSharesResult

    fun observeAllShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>>

    suspend fun getById(userId: UserId, shareId: ShareId): Share

    suspend fun updateVault(
        userId: UserId,
        shareId: ShareId,
        vault: NewVault
    ): Share

    suspend fun markAsPrimary(userId: UserId, shareId: ShareId): Share
}

data class RefreshSharesResult(
    val allShareIds: Set<ShareId>,
    val newShareIds: Set<ShareId>
)

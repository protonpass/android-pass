package me.proton.core.pass.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewVault

interface ShareRepository {
    suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): Result<Share>

    suspend fun refreshShares(userId: UserId): Result<Unit>
    fun observeShares(userId: SessionUserId): Flow<Result<List<Share>>>
    suspend fun getById(userId: UserId, shareId: ShareId): Result<Share?>
}

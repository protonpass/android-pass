package me.proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.NewVault

interface ShareRepository {
    suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): Result<Share>

    suspend fun refreshShares(userId: UserId): Result<List<Share>>
    fun observeShares(userId: SessionUserId): Flow<Result<List<Share>>>
    suspend fun getById(userId: UserId, shareId: ShareId): Result<Share?>
}

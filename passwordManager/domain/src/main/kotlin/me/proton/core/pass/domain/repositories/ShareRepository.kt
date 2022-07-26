package me.proton.core.pass.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewVault

interface ShareRepository {
    suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault,
    ): Share

    suspend fun refreshShares(userId: UserId)
    fun observeShares(userId: SessionUserId): Flow<List<Share>>
    suspend fun getById(userId: UserId, shareId: ShareId): Share?
}

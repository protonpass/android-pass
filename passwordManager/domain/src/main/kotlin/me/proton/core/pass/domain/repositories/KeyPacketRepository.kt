package me.proton.core.pass.domain.repositories

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.KeyPacket
import me.proton.core.pass.domain.ShareId

interface KeyPacketRepository {
    suspend fun getLatestKeyPacketForItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<KeyPacket>
}

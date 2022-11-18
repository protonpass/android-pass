package me.proton.android.pass.data.api.repositories

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.ShareId

interface KeyPacketRepository {
    suspend fun getLatestKeyPacketForItem(userId: UserId, shareId: ShareId, itemId: ItemId): Result<KeyPacket>
}

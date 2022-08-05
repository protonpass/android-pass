package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.responses.KeyPacketInfo
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

interface RemoteKeyPacketDataSource {
    suspend fun getLatestKeyPacketForItem(userId: UserId, shareId: ShareId, itemId: ItemId): KeyPacketInfo
}

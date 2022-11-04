package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.data.responses.KeyPacketInfo
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

interface RemoteKeyPacketDataSource {
    suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<KeyPacketInfo>
}

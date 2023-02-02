package proton.android.pass.data.api.repositories

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ItemId
import proton.pass.domain.KeyPacket
import proton.pass.domain.ShareId

interface KeyPacketRepository {
    suspend fun getLatestKeyPacketForItem(userId: UserId, shareId: ShareId, itemId: ItemId): LoadingResult<KeyPacket>
}

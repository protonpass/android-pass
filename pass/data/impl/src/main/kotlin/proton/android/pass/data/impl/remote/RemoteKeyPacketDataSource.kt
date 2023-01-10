package proton.android.pass.data.impl.remote

import proton.android.pass.data.impl.responses.KeyPacketInfo
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface RemoteKeyPacketDataSource {
    suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<KeyPacketInfo>
}

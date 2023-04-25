package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface RemoteItemKeyDataSource {
    suspend fun fetchLatestItemKey(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemLatestKeyResponse
}

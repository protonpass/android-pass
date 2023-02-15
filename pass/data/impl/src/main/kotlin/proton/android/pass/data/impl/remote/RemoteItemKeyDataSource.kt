package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

interface RemoteItemKeyDataSource {
    fun getLatestItemKey(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemLatestKeyResponse>
}

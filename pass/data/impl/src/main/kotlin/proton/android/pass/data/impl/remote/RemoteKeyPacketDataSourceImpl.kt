package proton.android.pass.data.impl.remote

import proton.android.pass.data.impl.responses.KeyPacketInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.toLoadingResult
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteKeyPacketDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteKeyPacketDataSource {
    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<KeyPacketInfo> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                getLatestKeyPacket(shareId.id, itemId.id).keyPacketInfo
            }
            .toLoadingResult()
}

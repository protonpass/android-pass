package me.proton.android.pass.data.impl.remote

import me.proton.android.pass.data.impl.responses.KeyPacketInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.toResult
import me.proton.pass.data.api.PasswordManagerApi
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteKeyPacketDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteKeyPacketDataSource {
    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<KeyPacketInfo> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                getLatestKeyPacket(shareId.id, itemId.id).keyPacketInfo
            }
            .toResult()
}

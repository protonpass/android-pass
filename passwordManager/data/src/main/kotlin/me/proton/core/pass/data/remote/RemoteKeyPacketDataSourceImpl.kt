package me.proton.core.pass.data.remote

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.responses.KeyPacketInfo
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

class RemoteKeyPacketDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteKeyPacketDataSource {
    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): KeyPacketInfo =
        api.get<PasswordManagerApi>(userId).invoke {
            getLatestKeyPacket(shareId.id, itemId.id).keyPacketInfo
        }.valueOrThrow
}

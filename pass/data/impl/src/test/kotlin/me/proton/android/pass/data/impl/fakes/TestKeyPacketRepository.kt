package me.proton.android.pass.data.impl.fakes

import me.proton.android.pass.data.api.repositories.KeyPacketRepository
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.ShareId

class TestKeyPacketRepository : KeyPacketRepository {

    private var result: Result<KeyPacket> = Result.Loading

    fun setResult(value: Result<KeyPacket>) {
        result = value
    }

    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<KeyPacket> = result
}

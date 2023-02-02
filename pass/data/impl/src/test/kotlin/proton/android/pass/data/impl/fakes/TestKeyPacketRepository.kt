package proton.android.pass.data.impl.fakes

import proton.android.pass.data.api.repositories.KeyPacketRepository
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ItemId
import proton.pass.domain.KeyPacket
import proton.pass.domain.ShareId

class TestKeyPacketRepository : KeyPacketRepository {

    private var result: LoadingResult<KeyPacket> = LoadingResult.Loading

    fun setResult(value: LoadingResult<KeyPacket>) {
        result = value
    }

    override suspend fun getLatestKeyPacketForItem(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): LoadingResult<KeyPacket> = result
}

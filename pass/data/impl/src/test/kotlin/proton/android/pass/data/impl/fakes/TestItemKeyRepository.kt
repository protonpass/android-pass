package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.ShareKey

class TestItemKeyRepository : ItemKeyRepository {

    private var getLatestItemKeyFlow = testFlow<Pair<ShareKey, ItemKey>>()

    fun emitGetLatestItemKey(value: Pair<ShareKey, ItemKey>) {
        getLatestItemKeyFlow.tryEmit(value)
    }

    override fun getLatestItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Pair<ShareKey, ItemKey>> = getLatestItemKeyFlow
}

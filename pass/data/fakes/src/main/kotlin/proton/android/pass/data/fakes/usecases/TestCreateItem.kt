package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.CreateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestCreateItem @Inject constructor() : CreateItem {

    private var item: Result<Item> = Result.failure(IllegalStateException("Result not set"))
    private var invoked = false

    fun hasBeenInvoked() = invoked

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): Item {
        invoked = true
        return item.getOrThrow()
    }

    fun sendItem(item: Result<Item>) {
        this.item = item
    }
}

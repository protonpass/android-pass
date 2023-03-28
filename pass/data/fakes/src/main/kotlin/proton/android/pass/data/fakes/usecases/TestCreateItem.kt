package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.usecases.CreateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestCreateItem @Inject constructor() : CreateItem {

    private var item: LoadingResult<Item> = LoadingResult.Loading
    private var invoked = false

    fun hasBeenInvoked() = invoked

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): LoadingResult<Item> {
        invoked = true
        return item
    }

    fun sendItem(item: LoadingResult<Item>) {
        this.item = item
    }
}

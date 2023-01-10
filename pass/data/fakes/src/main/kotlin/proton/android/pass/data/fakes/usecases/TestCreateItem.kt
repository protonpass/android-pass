package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.CreateItem
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import proton.pass.domain.entity.PackageName
import javax.inject.Inject

class TestCreateItem @Inject constructor() : CreateItem {

    private var item: Result<Item> = Result.Loading

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents,
        packageName: PackageName?
    ): Result<Item> = item

    fun sendItem(item: Result<Item>) {
        this.item = item
    }
}

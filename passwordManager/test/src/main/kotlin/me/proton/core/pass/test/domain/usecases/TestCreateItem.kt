package me.proton.core.pass.test.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem

class TestCreateItem : CreateItem {

    private var item: Result<Item> = Result.Loading

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): Result<Item> = item

    fun sendItem(item: Result<Item>) {
        this.item = item
    }
}

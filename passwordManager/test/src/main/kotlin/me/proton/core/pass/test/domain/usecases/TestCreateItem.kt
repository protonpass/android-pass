package me.proton.core.pass.test.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem

class TestCreateItem : CreateItem {

    var item: Item? = null

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): Item = item!!

    fun sendItem(item: Item) {
        this.item = item
    }
}

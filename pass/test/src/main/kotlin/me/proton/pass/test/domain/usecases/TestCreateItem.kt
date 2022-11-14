package me.proton.pass.test.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.usecases.CreateItem

class TestCreateItem : CreateItem {

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

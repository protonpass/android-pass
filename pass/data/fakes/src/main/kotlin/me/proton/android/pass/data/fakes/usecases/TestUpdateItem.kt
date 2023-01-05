package me.proton.android.pass.data.fakes.usecases

import me.proton.android.pass.data.api.usecases.UpdateItem
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class TestUpdateItem @Inject constructor() : UpdateItem {

    private var result: Result<Item> = Result.Loading

    fun setResult(result: Result<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        item: Item,
        contents: ItemContents
    ): Result<Item> = result
}

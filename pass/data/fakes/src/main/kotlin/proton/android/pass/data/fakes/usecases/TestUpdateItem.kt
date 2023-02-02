package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.UpdateItem
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestUpdateItem @Inject constructor() : UpdateItem {

    private var result: LoadingResult<Item> = LoadingResult.Loading

    fun setResult(result: LoadingResult<Item>) {
        this.result = result
    }

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        item: Item,
        contents: ItemContents
    ): LoadingResult<Item> = result
}

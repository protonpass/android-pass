package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.MigrateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestMigrateItem @Inject constructor() : MigrateItem {

    private var result: Result<Item> = Result.failure(IllegalStateException("Result not set"))

    fun setResult(value: Result<Item>) {
        result = value
    }

    override suspend fun invoke(
        sourceShare: ShareId,
        itemId: ItemId,
        destinationShare: ShareId
    ): Item = result.fold(
        onSuccess = { it },
        onFailure = { throw it }
    )
}

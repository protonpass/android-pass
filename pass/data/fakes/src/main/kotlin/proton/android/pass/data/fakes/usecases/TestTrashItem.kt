package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.TrashItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestTrashItem @Inject constructor() : TrashItem {

    private var result: Result<Unit> = Result.failure(IllegalStateException("TestTrashItem.result not set"))

    fun setResult(result: Result<Unit>) {
        this.result = result
    }

    override suspend fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId) =
        result.getOrThrow()
}

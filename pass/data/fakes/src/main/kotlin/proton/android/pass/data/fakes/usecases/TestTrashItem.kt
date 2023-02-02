package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.TrashItem
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestTrashItem @Inject constructor() : TrashItem {

    private var result: LoadingResult<Unit> = LoadingResult.Loading

    fun setResult(result: LoadingResult<Unit>) {
        this.result = result
    }

    override suspend fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId): LoadingResult<Unit> =
        result
}

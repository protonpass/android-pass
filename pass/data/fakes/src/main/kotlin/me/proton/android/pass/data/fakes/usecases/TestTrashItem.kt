package me.proton.android.pass.data.fakes.usecases

import me.proton.android.pass.data.api.usecases.TrashItem
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class TestTrashItem @Inject constructor() : TrashItem {

    private var result: Result<Unit> = Result.Loading

    fun setResult(result: Result<Unit>) {
        this.result = result
    }

    override suspend fun invoke(userId: UserId, shareId: ShareId, itemId: ItemId): Result<Unit> =
        result
}

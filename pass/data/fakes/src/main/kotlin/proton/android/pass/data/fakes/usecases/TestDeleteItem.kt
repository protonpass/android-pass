package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.DeleteItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestDeleteItem @Inject constructor() : DeleteItem {

    private var result: Result<Unit> = Result.success(Unit)
    private val memory: MutableList<Payload> = mutableListOf()

    fun setResult(value: Result<Unit>) {
        result = value
    }
    fun memory(): List<Payload> = memory

    override suspend fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId) {
        memory.add(Payload(userId, shareId, itemId))
        result.fold(
            onSuccess = {},
            onFailure = { throw it }
        )
    }

    data class Payload(
        val userId: UserId?,
        val shareId: ShareId,
        val itemId: ItemId
    )
}

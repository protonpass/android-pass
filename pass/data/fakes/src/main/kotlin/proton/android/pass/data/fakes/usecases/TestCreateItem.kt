package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.CreateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCreateItem @Inject constructor() : CreateItem {

    private var item: Result<Item> = Result.failure(IllegalStateException("Result not set"))

    private val memory = mutableListOf<Payload>()

    fun hasBeenInvoked() = memory.isNotEmpty()
    fun memory(): List<Payload> = memory

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): Item {
        memory.add(Payload(userId, shareId, itemContents))
        return item.getOrThrow()
    }

    fun sendItem(item: Result<Item>) {
        this.item = item
    }

    data class Payload(
        val userId: UserId,
        val shareId: ShareId,
        val itemContents: ItemContents
    )
}

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class DeleteItemImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val itemRepository: ItemRepository
) : DeleteItem {

    override suspend fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId) {
        val id = if (userId == null) {
            val user = requireNotNull(observeCurrentUser().first())
            user.userId
        } else {
            userId
        }
        itemRepository.deleteItem(id, shareId, itemId)
    }
}

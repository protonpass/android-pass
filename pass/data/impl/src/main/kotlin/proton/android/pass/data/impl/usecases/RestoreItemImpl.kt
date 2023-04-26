package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.RestoreItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class RestoreItemImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val itemRepository: ItemRepository
) : RestoreItem {

    override suspend fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId) {
        val id = if (userId == null) {
            val user = requireNotNull(observeCurrentUser().first())
            user.userId
        } else {
            userId
        }
        itemRepository.untrashItem(id, shareId, itemId)
    }
}

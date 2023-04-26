package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.RestoreItems
import javax.inject.Inject

class RestoreItemsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val itemRepository: ItemRepository
) : RestoreItems {

    override suspend fun invoke(userId: UserId?) {
        val id = if (userId == null) {
            val user = requireNotNull(observeCurrentUser().first())
            user.userId
        } else {
            userId
        }
        itemRepository.restoreItems(id)
    }
}

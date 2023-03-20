package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.log.api.PassLogger
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
        deleteItem(id, shareId, itemId)
    }

    private suspend fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId) {
        when (val res = itemRepository.deleteItem(userId, shareId, itemId)) {
            LoadingResult.Loading -> {}
            is LoadingResult.Success -> {}
            is LoadingResult.Error -> {
                PassLogger.w(TAG, res.exception, "Error deleting item")
                throw res.exception
            }
        }
    }

    companion object {
        private const val TAG = "DeleteItemImpl"
    }
}

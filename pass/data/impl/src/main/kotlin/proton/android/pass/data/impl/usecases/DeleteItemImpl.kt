package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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

    override fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId): Flow<Unit> =
        if (userId == null) {
            observeCurrentUser()
                .flatMapLatest { deleteItem(it.userId, shareId, itemId) }
        } else {
            deleteItem(userId, shareId, itemId)
        }

    private fun deleteItem(userId: UserId, shareId: ShareId, itemId: ItemId): Flow<Unit> = flow {
        when (val res = itemRepository.deleteItem(userId, shareId, itemId)) {
            LoadingResult.Loading -> {}
            is LoadingResult.Error -> {
                PassLogger.w(TAG, res.exception, "Error deleting item")
                throw res.exception
            }
            is LoadingResult.Success -> {
                emit(Unit)
            }
        }
    }

    companion object {
        private const val TAG = "DeleteItemImpl"
    }
}

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.log.api.PassLogger
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
        restoreItems(id)
    }

    private suspend fun restoreItems(userId: UserId) {
        when (val res = itemRepository.restoreItems(userId)) {
            LoadingResult.Loading -> {}
            is LoadingResult.Success -> {}
            is LoadingResult.Error -> {
                PassLogger.w(TAG, res.exception, "Error restoring items")
                throw res.exception
            }
        }
    }

    companion object {
        private const val TAG = "RestoreItemsImpl"
    }
}

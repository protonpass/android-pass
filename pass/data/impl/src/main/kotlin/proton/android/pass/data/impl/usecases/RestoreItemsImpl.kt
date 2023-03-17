package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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

    override fun invoke(userId: UserId?): Flow<Unit> =
        if (userId == null) {
            observeCurrentUser()
                .flatMapLatest { restoreItems(it.userId) }
        } else {
            restoreItems(userId)
        }

    private fun restoreItems(userId: UserId): Flow<Unit> = flow {
        when (val res = itemRepository.restoreItems(userId)) {
            LoadingResult.Loading -> {}
            is LoadingResult.Error -> {
                PassLogger.w(TAG, res.exception, "Error restoring items")
                throw res.exception
            }
            is LoadingResult.Success -> {
                emit(Unit)
            }
        }
    }

    companion object {
        private const val TAG = "RestoreItemsImpl"
    }
}

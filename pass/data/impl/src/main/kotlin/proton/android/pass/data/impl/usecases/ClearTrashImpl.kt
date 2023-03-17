package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ClearTrash
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ClearTrashImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val itemRepository: ItemRepository
) : ClearTrash {

    override fun invoke(userId: UserId?): Flow<Unit> =
        if (userId == null) {
            observeCurrentUser()
                .flatMapLatest { clearTrash(it.userId) }
        } else {
            clearTrash(userId)
        }

    private fun clearTrash(userId: UserId): Flow<Unit> = flow {
        when (val res = itemRepository.clearTrash(userId)) {
            LoadingResult.Loading -> {}
            is LoadingResult.Error -> {
                PassLogger.w(TAG, res.exception, "Error clearing trash")
                throw res.exception
            }
            is LoadingResult.Success -> {
                emit(Unit)
            }
        }
    }

    companion object {
        private const val TAG = "ClearTrashImpl"
    }
}

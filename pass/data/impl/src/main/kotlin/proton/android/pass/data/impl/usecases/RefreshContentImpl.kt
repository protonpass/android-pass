package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.firstError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RefreshContentImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : RefreshContent {

    override suspend fun invoke(userId: UserId) {
        PassLogger.i(TAG, "Refreshing shares")
        val refreshSharesResult = shareRepository.refreshShares(userId)
        return coroutineScope {
            PassLogger.i(TAG, "Refreshing items for shares")
            val refreshItemsResults = refreshSharesResult.allShareIds.map { share ->
                async { runCatching { itemRepository.refreshItems(userId, share) } }
            }.awaitAll()

            val firstError = refreshItemsResults.firstError()
            PassLogger.i(TAG, "Items refreshed [success=${firstError == null}]")
            if (firstError != null) {
                throw firstError
            }
        }
    }

    companion object {
        private const val TAG = "RefreshContentImpl"
    }
}


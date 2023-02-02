package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.log.api.PassLogger
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import javax.inject.Inject

class RefreshContentImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : RefreshContent {

    override suspend fun invoke(userId: UserId): LoadingResult<Unit> {
        PassLogger.i(TAG, "Refreshing shares")
        return shareRepository.refreshShares(userId)
            .map { shares ->
                coroutineScope {
                    PassLogger.i(TAG, "Refreshing items for shares")
                    val refreshItemsResults = shares.map { share ->
                        async { itemRepository.refreshItems(userId, share) }
                    }.awaitAll()

                    val firstError = refreshItemsResults.firstOrNull { it is LoadingResult.Error }
                    PassLogger.i(TAG, "Items refreshed [success=${firstError == null}]")
                    if (firstError != null) {
                        firstError as LoadingResult.Error
                    } else {
                        LoadingResult.Success(Unit)
                    }
                }
            }
    }

    companion object {
        private const val TAG = "RefreshContentImpl"
    }
}


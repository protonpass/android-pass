package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.repositories.ShareRepository
import me.proton.android.pass.data.api.usecases.RefreshContent
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import javax.inject.Inject

class RefreshContentImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : RefreshContent {

    override suspend fun invoke(userId: UserId): Result<Unit> =
        shareRepository.refreshShares(userId)
            .map { shares ->
                coroutineScope {
                    val refreshItemsResults = shares.map { share ->
                        async { itemRepository.refreshItems(userId, share) }
                    }.awaitAll()

                    val firstError = refreshItemsResults.firstOrNull { it is Result.Error }
                    if (firstError != null) {
                        firstError as Result.Error
                    } else {
                        Result.Success(Unit)
                    }
                }
            }
}


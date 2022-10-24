package me.proton.pass.domain.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.domain.repositories.ItemRepository
import me.proton.pass.domain.repositories.ShareRepository
import javax.inject.Inject

interface RefreshContent {
    suspend operator fun invoke(userId: UserId): Result<Unit>
}

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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Item
import proton.pass.domain.ItemState
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class MigrateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : MigrateVault {

    override suspend fun invoke(origin: ShareId, dest: ShareId) {
        val userId = requireNotNull(accountManager.getPrimaryUserId().firstOrNull())
        onUserIdReceived(userId, origin, dest)
    }

    private suspend fun onUserIdReceived(
        userId: UserId,
        origin: ShareId,
        dest: ShareId
    ) {
        val items = itemRepository.observeItems(
            userId = userId,
            shareSelection = ShareSelection.Share(origin),
            itemState = ItemState.Active,
            itemTypeFilter = ItemTypeFilter.All
        ).first()

        val sourceShare = shareRepository.getById(userId, origin)
        val destShare = shareRepository.getById(userId, dest)
        performMigrate(items, userId, sourceShare, destShare)
    }

    private suspend fun performMigrate(
        items: List<Item>,
        userId: UserId,
        sourceShare: Share,
        destShare: Share
    ) {
        withContext(Dispatchers.IO) {
            migrateItems(
                items = items,
                coroutineScope = this,
                userId = userId,
                sourceShare = sourceShare,
                destShare = destShare
            )
        }
    }

    private suspend fun migrateItems(
        items: List<Item>,
        coroutineScope: CoroutineScope,
        userId: UserId,
        sourceShare: Share,
        destShare: Share
    ) {
        val results = items
            .map { item ->
                coroutineScope.async {
                    runCatching {
                        itemRepository.migrateItem(
                            userId = userId,
                            source = sourceShare,
                            destination = destShare,
                            itemId = item.id
                        )
                    }
                }
            }
            .awaitAll()

        val firstFailure = results.firstOrNull { it.isFailure } ?: Result.success(Unit)
        firstFailure.onFailure {
            PassLogger.d(TAG, it, "Failed to migrate vault")
            throw it
        }
    }
    companion object {
        private const val TAG = "MigrateVaultImpl"
    }
}

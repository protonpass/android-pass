package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId

@HiltWorker
open class FetchItemsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting FetchItemsWorker")

        val userId = accountManager.getPrimaryUserId().first() ?: return Result.failure()
        val shareIds = inputData.getStringArray(ARG_SHARE_IDS)?.map { ShareId(it) } ?: emptyList()

        itemSyncStatusRepository.emit(ItemSyncStatus.Syncing)
        val results = withContext(Dispatchers.IO) {
            shareIds.map { shareId ->
                async {
                    PassLogger.d(TAG, "Refreshing items on share ${shareId.id}")
                    val res = itemRepository.refreshItems(userId, shareId)
                    PassLogger.d(TAG, "Refreshed items on share ${shareId.id}")
                    res
                }
            }.awaitAll()
        }

        PassLogger.i(TAG, "Finished refreshing items")

        val hasErrors = results.any { it is LoadingResult.Error }
        if (hasErrors) {
            itemSyncStatusRepository.emit(ItemSyncStatus.NotSynced)
            return Result.retry()
        }

        val hasItems = results.any { it is LoadingResult.Success && it.data.isNotEmpty() }
        itemSyncStatusRepository.emit(ItemSyncStatus.Synced(hasItems))
        return Result.success()
    }

    companion object {
        private const val TAG = "FetchItemsWorker"
        private const val ARG_SHARE_IDS = "share_ids"

        fun getRequestFor(shareIds: List<ShareId>): WorkRequest {
            val shareIdsAsString = shareIds.map { it.id }.toTypedArray()
            val extras = mutableMapOf(ARG_SHARE_IDS to shareIdsAsString)

            val data = Data.Builder()
                .putAll(extras.toMap())
                .build()

            return OneTimeWorkRequestBuilder<FetchItemsWorker>()
                .setInputData(data)
                .build()
        }
    }
}

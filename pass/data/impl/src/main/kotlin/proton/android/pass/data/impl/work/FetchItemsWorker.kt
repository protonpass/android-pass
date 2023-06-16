/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
                    runCatching {
                        itemRepository.refreshItems(userId, shareId)
                    }.onSuccess {
                        PassLogger.d(TAG, "Refreshed items on share ${shareId.id} (has ${it.size} items)")
                    }.onFailure {
                        PassLogger.e(TAG, it, "Error refreshing items on share ${shareId.id}")
                    }
                }
            }.awaitAll()
        }

        PassLogger.i(TAG, "Finished refreshing items")

        val items = results.map { result ->
            result.fold(
                onSuccess = { it },
                onFailure = {
                    itemSyncStatusRepository.emit(ItemSyncStatus.NotSynced)
                    return Result.retry()
                }
            )
        }.flatten()

        val hasItems = results.any { items.isNotEmpty() }
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

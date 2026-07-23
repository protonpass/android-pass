/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.usecases.sync

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatus.SyncError.CryptoError
import proton.android.pass.data.api.repositories.ItemSyncStatus.SyncError.DownloadError
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.VaultProgress
import proton.android.pass.data.api.usecases.folders.RefreshFolders
import proton.android.pass.data.api.usecases.sync.ForceSyncItems
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ForceSyncItemsImpl @Inject constructor(
    private val refreshFolders: RefreshFolders,
    private val itemRepository: ItemRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val searchIndexRepository: SearchIndexRepository
) : ForceSyncItems {

    @SuppressWarnings("LongMethod")
    override suspend fun invoke(
        userId: UserId,
        shareIds: Set<ShareId>,
        hasInactiveShares: Boolean,
        hasInvalidGroupShares: Boolean
    ): ForceSyncResult {
        if (shareIds.isEmpty()) return ForceSyncResult.Success

        refreshFolders(userId, shareIds)

        val results: List<Result<Pair<ShareId, List<ItemRevision>>>> = runConcurrently(
            items = shareIds,
            block = { shareId ->
                val shareItems = itemRepository.downloadItemsAndObserveProgress(
                    userId = userId,
                    shareId = shareId,
                    onProgress = { progress ->
                        itemSyncStatusRepository.emit(
                            ItemSyncStatus.SyncDownloading(
                                shareId = shareId,
                                current = progress.current,
                                total = progress.total
                            )
                        )
                    }
                )

                shareId to shareItems
            }
        )

        val successes = results.mapNotNull { it.getOrNull() }
        val downloadFailedShareIds = shareIds - successes.map { it.first }.toSet()
        val itemsToInsert: Map<ShareId, List<ItemRevision>> = successes.toMap()

        val setShareItemsResult = itemRepository.setShareItems(
            userId = userId,
            items = itemsToInsert,
            onProgress = { progress: VaultProgress ->
                itemSyncStatusRepository.emit(
                    ItemSyncStatus.SyncInserting(
                        current = progress.current,
                        total = progress.total
                    )
                )
            }
        )
        for (shareId in setShareItemsResult.failedShareIds) {
            val insertedCount = setShareItemsResult.insertedCountByShare[shareId] ?: 0
            itemSyncStatusRepository.emit(
                ItemSyncStatus.SyncDownloading(
                    shareId = shareId,
                    current = insertedCount,
                    total = insertedCount
                )
            )
        }
        val failedShareIds: Set<ShareId> = downloadFailedShareIds + setShareItemsResult.failedShareIds

        val result = when {
            failedShareIds.isEmpty() -> {
                // Rebuild search index after successful insertion
                runCatching {
                    PassLogger.i(TAG, "Starting search index rebuild")
                    searchIndexRepository.rebuildIndex(userId)
                    PassLogger.i(TAG, "Search index rebuild completed")
                }.onFailure { error ->
                    PassLogger.w(
                        TAG,
                        "Search index rebuild failed: ${error::class.simpleName}"
                    )
                }
                itemSyncStatusRepository.emit(
                    status = ItemSyncStatus.SyncSuccess(
                        hasInactiveShares = hasInactiveShares,
                        hasInvalidGroupShares = hasInvalidGroupShares
                    )
                )
                ForceSyncResult.Success
            }

            downloadFailedShareIds.isEmpty() -> {
                // All downloads succeeded; failures are crypto-only (permanent, non-retriable)
                itemSyncStatusRepository.emit(CryptoError(failedShareIds = failedShareIds))
                ForceSyncResult.PartialSuccess
            }

            else -> {
                itemSyncStatusRepository.emit(DownloadError(failedShareIds = downloadFailedShareIds))
                ForceSyncResult.Error
            }
        }

        itemSyncStatusRepository.setMode(SyncMode.Background)
        return result
    }

    private companion object {

        private const val TAG = "ForceSyncItemsImpl"

    }

}

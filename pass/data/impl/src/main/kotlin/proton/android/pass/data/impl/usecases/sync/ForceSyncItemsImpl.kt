/*
 * Copyright (c) 2024 Proton AG
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
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.VaultProgress
import proton.android.pass.data.api.usecases.sync.ForceSyncItems
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ForceSyncItemsImpl @Inject constructor(
    private val itemRepository: ItemRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository
) : ForceSyncItems {

    override suspend fun invoke(
        userId: UserId,
        shareIds: List<ShareId>,
        isBackground: Boolean
    ): ForceSyncResult {
        if (shareIds.isEmpty()) return ForceSyncResult.Success

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

                        PassLogger.d(TAG, "Downloading Share ${shareId.id} progress: $progress")
                    }
                )

                shareId to shareItems
            },
            onSuccess = { shareId, _ ->
                PassLogger.d(TAG, "Downloaded Share ${shareId.id} successfully")
            },
            onFailure = { shareId, err ->
                PassLogger.w(TAG, "Error downloading items on share ${shareId.id}")
                PassLogger.w(TAG, err)
            }
        )

        val (successes, errors) = results.partition { it.isSuccess }

        val itemsToInsert: Map<ShareId, List<ItemRevision>> = successes
            .mapNotNull { it.getOrNull() }
            .toMap()

        itemRepository.setShareItems(
            userId = userId,
            items = itemsToInsert,
            onProgress = { progress: VaultProgress ->
                itemSyncStatusRepository.emit(
                    ItemSyncStatus.SyncInserting(
                        current = progress.current,
                        total = progress.total
                    )
                )

                PassLogger.d(TAG, "Inserting ${progress.current} of: ${progress.total}")
            }
        )

        val result = if (errors.isEmpty()) {
            itemSyncStatusRepository.emit(ItemSyncStatus.SyncSuccess)
            ForceSyncResult.Success
        } else {
            itemSyncStatusRepository.emit(ItemSyncStatus.SyncError)
            ForceSyncResult.Error
        }

        itemSyncStatusRepository.setMode(SyncMode.Background)
        return result
    }

    private companion object {

        private const val TAG = "ForceSyncItemsImpl"

    }

}

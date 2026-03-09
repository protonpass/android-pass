/*
 * Copyright (c) 2024-2026 Proton AG
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

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatus.SyncError.CryptoError
import proton.android.pass.data.api.repositories.ItemSyncStatus.SyncError.DownloadError
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.VaultProgress
import proton.android.pass.data.api.usecases.folders.RefreshFolders
import proton.android.pass.data.api.usecases.sync.ForceSyncItems
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

class ForceSyncItemsImpl @Inject constructor(
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val refreshFolders: RefreshFolders,
    private val itemRepository: ItemRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository
) : ForceSyncItems {

    @SuppressWarnings("LongMethod")
    override suspend fun invoke(
        userId: UserId,
        shareIds: Set<ShareId>,
        hasInactiveShares: Boolean,
        hasInvalidGroupShares: Boolean,
        hasInvalidAddressShares: Boolean
    ): ForceSyncResult {
        if (shareIds.isEmpty()) return ForceSyncResult.Success

        val isFoldersEnabled: Boolean =
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_FOLDERS).first()
        if (isFoldersEnabled) {
            safeRunCatching {
                refreshFolders(userId, shareIds)
            }.onFailure {
                PassLogger.w(TAG, "Failed to refresh folders for ids $shareIds")
                PassLogger.w(TAG, it)
            }
        }
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

        val successes = results.mapNotNull { it.getOrNull() }
        val downloadFailedShareIds = shareIds - successes.map { it.first }.toSet()

        val itemsToInsert: Map<ShareId, List<ItemRevision>> = successes.toMap()

        val setShareItemsFailedShareIds = itemRepository.setShareItems(
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
        val failedShareIds: Set<ShareId> = downloadFailedShareIds + setShareItemsFailedShareIds

        val result = when {
            failedShareIds.isEmpty() -> {
                itemSyncStatusRepository.emit(
                    status = ItemSyncStatus.SyncSuccess(
                        hasInactiveShares = hasInactiveShares,
                        hasInvalidGroupShares = hasInvalidGroupShares,
                        hasInvalidAddressShares = hasInvalidAddressShares
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

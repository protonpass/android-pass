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

package proton.android.pass.data.impl.usecases.folders

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.folders.FolderPresence
import proton.android.pass.data.api.usecases.folders.HasAnyFolders
import proton.android.pass.data.impl.remote.RemoteFolderDataSource
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

/**
 * Asks the server whether the user has any folders, without writing anything locally. Cannot be
 * answered from the database: an affected user has an empty local folder table, which is the bug.
 */
class HasAnyFoldersImpl @Inject constructor(
    private val remoteFolderDataSource: RemoteFolderDataSource,
    private val shareRepository: ShareRepository
) : HasAnyFolders {

    override suspend fun invoke(userId: UserId, shareIds: Set<ShareId>): FolderPresence {
        if (shareIds.isEmpty()) {
            PassLogger.i(TAG, "No shares to check for folders")
            return FolderPresence.Unknown
        }

        val vaultShareIds = shareRepository.filterShareIdsByType(
            userId = userId,
            shareIds = shareIds,
            shareType = ShareType.Vault
        )

        if (vaultShareIds.isEmpty()) {
            PassLogger.i(TAG, "No vault shares to check for folders")
            return FolderPresence.Unknown
        }

        var hasFailures = false

        vaultShareIds.chunked(BATCH_SIZE).forEach { batch ->
            val results = runConcurrently(
                maxParallelCalls = BATCH_SIZE,
                items = batch,
                block = { shareId -> remoteFolderDataSource.countFolders(userId, shareId) },
                onFailure = { shareId, error ->
                    PassLogger.w(TAG, "Failed to count folders for shareId=${shareId.id}")
                    PassLogger.w(TAG, error)
                }
            )

            if (results.any { result -> result.getOrDefault(0L) > 0L }) {
                return FolderPresence.HasFolders
            }

            if (results.any { result -> result.isFailure }) {
                hasFailures = true
            }
        }

        return if (hasFailures) {
            PassLogger.i(TAG, "Folder check incomplete, treating as unknown")
            FolderPresence.Unknown
        } else {
            FolderPresence.NoFolders
        }
    }

    private companion object {

        private const val TAG = "HasAnyFoldersImpl"

        private const val BATCH_SIZE = 3

    }
}

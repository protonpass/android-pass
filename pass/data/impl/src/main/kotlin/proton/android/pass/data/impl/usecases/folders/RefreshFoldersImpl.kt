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
import proton.android.pass.data.api.repositories.FolderRepository
import proton.android.pass.data.api.usecases.folders.RefreshFolders
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RefreshFoldersImpl @Inject constructor(
    private val folderRepository: FolderRepository
) : RefreshFolders {

    override suspend fun invoke(userId: UserId, shareIds: Set<ShareId>) {
        if (shareIds.isEmpty()) return

        val results = runConcurrently(
            maxParallelCalls = MAX_PARALLEL_REFRESHES,
            items = shareIds,
            block = { shareId -> folderRepository.refreshFolders(userId, shareId) },
            onFailure = { shareId, error ->
                PassLogger.w(TAG, "Failed to refresh folders for shareId=${shareId.id}")
                PassLogger.w(TAG, error)
            }
        )

        results.firstOrNull { it.isFailure }?.exceptionOrNull()?.let { throw it }
    }

    private companion object {
        private const val TAG = "RefreshFoldersImpl"
        private const val MAX_PARALLEL_REFRESHES = 3
    }
}

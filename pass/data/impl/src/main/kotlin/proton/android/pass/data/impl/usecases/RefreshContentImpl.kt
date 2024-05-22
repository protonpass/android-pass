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

package proton.android.pass.data.impl.usecases

import androidx.work.WorkManager
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RefreshContentImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val workManager: WorkManager,
    private val syncStatusRepository: ItemSyncStatusRepository
) : RefreshContent {

    override suspend fun invoke() {
        PassLogger.i(TAG, "Refreshing shares")
        syncStatusRepository.clear()
        syncStatusRepository.setMode(SyncMode.ShownToUser)
        syncStatusRepository.emit(ItemSyncStatus.SyncStarted)

        runCatching {
            accountManager.getPrimaryUserId()
                .firstOrNull()
                ?.let { userId -> shareRepository.refreshShares(userId) }
                ?: throw UserIdNotAvailableError()
        }
            .onFailure { error ->
                PassLogger.w(TAG, "Error refreshing shares")
                PassLogger.w(TAG, error)
                syncStatusRepository.emit(ItemSyncStatus.SyncError)
                throw error
            }
            .onSuccess { refreshSharesResult ->
                FetchItemsWorker.getRequestFor(
                    source = FetchItemsWorker.FetchSource.ForceSync,
                    shareIds = refreshSharesResult.allShareIds.toList()
                ).also(workManager::enqueue)
            }
    }

    private companion object {

        private const val TAG = "RefreshContentImpl"

    }

}

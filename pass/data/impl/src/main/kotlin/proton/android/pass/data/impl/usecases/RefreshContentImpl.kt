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

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RefreshContentImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val syncStatusRepository: ItemSyncStatusRepository,
    private val refreshSharesAndEnqueueSync: RefreshSharesAndEnqueueSync
) : RefreshContent {

    override suspend fun invoke(userId: UserId?) {
        PassLogger.i(TAG, "Refreshing shares")
        syncStatusRepository.clear()
        syncStatusRepository.setMode(SyncMode.ShownToUser)
        syncStatusRepository.emit(ItemSyncStatus.SyncStarted)

        val actualUserId = userId ?: accountManager.getPrimaryUserId().firstOrNull()
            ?: throw UserIdNotAvailableError()

        runCatching {
            refreshSharesAndEnqueueSync(
                userId = actualUserId,
                syncType = RefreshSharesAndEnqueueSync.SyncType.FULL
            )
        }.onFailure { error ->
            PassLogger.w(TAG, "Error in RefreshSharesAndEnqueueSync")
            PassLogger.w(TAG, error)
            syncStatusRepository.emit(ItemSyncStatus.SyncError)
            throw error
        }
    }

    private companion object {

        private const val TAG = "RefreshContentImpl"

    }

}

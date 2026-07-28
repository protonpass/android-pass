/*
 * Copyright (c) 2023-2026 Proton AG
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

import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.api.repositories.PasswordHistoryEntryRepository
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.ClearUserSyncState
import proton.android.pass.data.impl.db.DatabaseCleanupHelper
import proton.android.pass.data.impl.repositories.ExtraPasswordRepository
import proton.android.pass.log.api.LogFileManager
import javax.inject.Inject

class ClearUserDataImpl @Inject constructor(
    private val clearUserSyncState: ClearUserSyncState,
    private val extraPasswordRepository: ExtraPasswordRepository,
    private val passwordHistoryEntryRepository: PasswordHistoryEntryRepository,
    private val databaseCleanupHelper: DatabaseCleanupHelper,
    private val logFileManager: LogFileManager,

    private val appDispatchers: AppDispatchers
) : ClearUserData {

    override suspend fun invoke(userId: UserId) {
        withContext(appDispatchers.io) {
            clearUserSyncState(userId)
            extraPasswordRepository.removeLocalExtraPasswordForUser(userId)
            passwordHistoryEntryRepository.deletePasswordHistoryEntryForUser(userId)
            databaseCleanupHelper.cleanupUserData()
            val logFile = logFileManager.getLogFile(userId)
            logFileManager.deleteLogFile(logFile)
        }
    }

}

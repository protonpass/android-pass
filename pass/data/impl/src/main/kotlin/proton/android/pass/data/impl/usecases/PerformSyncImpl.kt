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

import kotlinx.coroutines.withTimeout
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.SyncUserEvents
import proton.android.pass.data.api.usecases.sync.CheckFolderForceSync
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration.Companion.minutes

class PerformSyncImpl @Inject constructor(
    private val syncUserEvents: SyncUserEvents,
    private val checkFolderForceSync: Provider<CheckFolderForceSync>
) : PerformSync {

    override suspend fun invoke(
        userId: UserId,
        forceSync: Boolean,
        trigger: String,
        syncReason: SyncReason,
        syncMode: SyncMode
    ) {
        PassLogger.i(
            TAG,
            "Performing sync for $userId started (forceSync=$forceSync, trigger=$trigger, " +
                "syncMode=$syncMode)"
        )

        performSyncWithPendingEvents(userId, forceSync, trigger, syncReason, syncMode)

        if (syncReason != SyncReason.FolderRepair) {
            safeRunCatching { checkFolderForceSync.get().invoke(userId, syncMode) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error checking whether a folders force sync is owed")
                    PassLogger.w(TAG, error)
                }
        }

        PassLogger.i(TAG, "Performing sync for $userId finished")
    }

    private suspend fun performSyncWithPendingEvents(
        userId: UserId,
        forceSync: Boolean,
        trigger: String,
        syncReason: SyncReason,
        syncMode: SyncMode
    ) {
        performSyncUserEvents(userId, forceSync, trigger, syncReason, syncMode)
            .exceptionOrNull()
            ?.let { error -> PassLogger.w(TAG, "Performing sync error: ${error.message}") }
    }

    private suspend fun performSyncUserEvents(
        userId: UserId,
        forceSync: Boolean,
        trigger: String,
        syncReason: SyncReason,
        syncMode: SyncMode
    ): Result<Unit> = runCatching {
        withTimeout(2.minutes) {
            syncUserEvents(userId, forceSync, trigger, syncReason, syncMode)
            PassLogger.i(TAG, "User events sync for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "User events sync for $userId error: ${error.message}")
    }

    private companion object {

        private const val TAG = "PerformSyncImpl"

    }

}

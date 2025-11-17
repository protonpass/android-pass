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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class PerformSyncImpl @Inject constructor(
    private val applyPendingEvents: ApplyPendingEvents,
    private val refreshInvites: RefreshInvites,
    private val syncPendingAliases: SyncSimpleLoginPendingAliases,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : PerformSync {

    override suspend fun invoke(userId: UserId) {
        PassLogger.i(TAG, "Performing sync for $userId started")

        val isUserEventsEnabled = featureFlagsPreferencesRepository
            .get<Boolean>(FeatureFlag.PASS_USER_EVENTS_V1)
            .first()

        if (isUserEventsEnabled) {
            performSyncWithUserEvents(userId)
        } else {
            performSyncWithPendingEvents(userId)
        }

        PassLogger.i(TAG, "Performing sync for $userId finished")
    }

    private fun performSyncWithUserEvents(userId: UserId) {
        PassLogger.i(TAG, "Performing sync with user events for $userId")
    }

    private suspend fun performSyncWithPendingEvents(userId: UserId) = coroutineScope {
        val tasks = listOf(
            async { performPendingEvents(userId) },
            async { performRefreshInvites(userId) },
            async { syncPendingSlAliases(userId) }
        )

        val results = awaitAll(*tasks.toTypedArray())

        results.firstOrNull { it.isFailure }?.let { result ->
            result.exceptionOrNull()?.let { error ->
                PassLogger.w(TAG, "Performing sync error: ${error.message}")
            }
        }
    }

    private suspend fun performPendingEvents(userId: UserId): Result<Unit> = runCatching {
        withTimeout(2.minutes) {
            applyPendingEvents(userId)
            PassLogger.i(TAG, "Pending events for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Pending events for $userId error: ${error.message}")
    }

    private suspend fun performRefreshInvites(userId: UserId): Result<Unit> = runCatching {
        withTimeout(2.minutes) {
            refreshInvites(userId)
            PassLogger.i(TAG, "Refresh invites for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Refresh invites for $userId error: ${error.message}")
    }

    private suspend fun syncPendingSlAliases(userId: UserId): Result<Unit> = runCatching {
        withTimeout(2.minutes) {
            syncPendingAliases(userId)
            PassLogger.i(TAG, "Pending SL aliases sync for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Pending SL aliases sync for $userId error: ${error.message}")
    }

    private companion object {

        private const val TAG = "PerformSyncImpl"

    }

}

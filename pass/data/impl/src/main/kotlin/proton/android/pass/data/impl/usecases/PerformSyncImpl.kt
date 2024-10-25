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
import kotlinx.coroutines.withTimeout
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class PerformSyncImpl @Inject constructor(
    private val applyPendingEvents: ApplyPendingEvents,
    private val refreshInvites: RefreshInvites,
    private val syncPendingAliases: SyncSimpleLoginPendingAliases
) : PerformSync {

    override suspend fun invoke(userId: UserId) = coroutineScope {
        PassLogger.i(TAG, "Performing sync for $userId started")

        val pendingEvents = async { performPendingEvents(userId) }
        pendingEvents.invokeOnCompletion { error ->
            if (error == null) {
                PassLogger.i(TAG, "Pending events for $userId finished")
            } else {
                PassLogger.w(TAG, error)
                PassLogger.i(TAG, "Pending events for $userId error")
            }
        }

        val refreshInvites = async { performRefreshInvites(userId) }
        refreshInvites.invokeOnCompletion { error ->
            if (error == null) {
                PassLogger.i(TAG, "Refresh invites for $userId finished")
            } else {
                PassLogger.w(TAG, error)
                PassLogger.i(TAG, "Refresh invites for $userId error")
            }
        }

        val pendingSlAliases = async { syncPendingSlAliases(userId) }
        pendingSlAliases.invokeOnCompletion { error ->
            if (error == null) {
                PassLogger.i(TAG, "Pending SL aliases sync finished for $userId")
            } else {
                PassLogger.i(TAG, "Pending SL aliases sync error for $userId")
                PassLogger.w(TAG, error)
            }
        }

        awaitAll(pendingEvents, refreshInvites, pendingSlAliases)
    }
        .firstOrNull { syncResult -> syncResult.isFailure }
        ?.exceptionOrNull()
        ?.let { error -> PassLogger.w(TAG, "Performing sync error: ${error.message}") }
        ?: PassLogger.i(TAG, "Performing sync finished")

    private suspend fun performPendingEvents(userId: UserId): Result<Unit> =
        runCatching { withTimeout(2.minutes) { applyPendingEvents(userId) } }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )

    private suspend fun performRefreshInvites(userId: UserId): Result<Unit> =
        runCatching { withTimeout(2.minutes) { refreshInvites(userId) } }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )

    private suspend fun syncPendingSlAliases(userId: UserId): Result<Unit> =
        runCatching { withTimeout(2.minutes) { syncPendingAliases(userId) } }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )

    private companion object {

        private const val TAG = "PerformSyncImpl"

    }

}

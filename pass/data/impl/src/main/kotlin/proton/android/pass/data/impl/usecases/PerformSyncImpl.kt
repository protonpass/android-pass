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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class PerformSyncImpl @Inject constructor(
    private val applyPendingEvents: ApplyPendingEvents,
    private val refreshInvites: RefreshInvites
) : PerformSync {

    override suspend fun invoke(userId: UserId?) {
        PassLogger.i(TAG, "Performing sync started")
        val res = withContext(Dispatchers.IO) {
            val pendingEvents = async { performPendingEvents(userId) }
            pendingEvents.invokeOnCompletion {
                if (it != null) {
                    PassLogger.w(TAG, it)
                    PassLogger.i(TAG, "Pending events error")
                }
                PassLogger.i(TAG, "Pending events finished")
            }
            val refreshInvites = async { performRefreshInvites(userId) }
            refreshInvites.invokeOnCompletion {
                if (it != null) {
                    PassLogger.w(TAG, it)
                    PassLogger.i(TAG, "Refresh invites error")
                } else {
                    PassLogger.i(TAG, "Refresh invites finished")
                }
            }
            awaitAll(pendingEvents, refreshInvites)
        }

        val exception = res.firstOrNull { it.isFailure }?.exceptionOrNull()
        if (exception != null) {
            PassLogger.w(TAG, exception)
            PassLogger.w(TAG, "Performing sync error")
            Result.failure(exception)
        } else {
            PassLogger.i(TAG, "Performing sync finished")
            Result.success(Unit)
        }
    }

    private suspend fun performPendingEvents(userId: UserId?): Result<Unit> =
        runCatching { withTimeout(2.minutes) { applyPendingEvents(userId) } }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )

    private suspend fun performRefreshInvites(userId: UserId?): Result<Unit> =
        runCatching { withTimeout(2.minutes) { refreshInvites(userId) } }
            .fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )

    companion object {
        private const val TAG = "PerformSyncImpl"
    }
}

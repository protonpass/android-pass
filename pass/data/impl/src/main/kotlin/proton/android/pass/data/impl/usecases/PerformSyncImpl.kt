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
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RefreshInvites
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class PerformSyncImpl @Inject constructor(
    private val applyPendingEvents: ApplyPendingEvents,
    private val refreshInvites: RefreshInvites
) : PerformSync {
    override suspend fun invoke(): Result<Unit> = withContext(Dispatchers.IO) {
        PassLogger.i(TAG, "Performing sync started")
        val pendingEvents = async { performPendingEvents() }
        val refreshInvites = async { performRefreshInvites() }
        val res = awaitAll(pendingEvents, refreshInvites)

        val exception = res.firstOrNull { it.isFailure }?.exceptionOrNull()
        if (exception != null) {
            PassLogger.i(TAG, "Performing sync error")
            return@withContext Result.failure(exception)
        } else {
            PassLogger.i(TAG, "Performing sync finished")
            Result.success(Unit)
        }
    }


    private suspend fun performPendingEvents(): Result<Unit> =
        runCatching { applyPendingEvents() }
            .fold(
                onSuccess = {
                    PassLogger.i(TAG, "Pending events finished")
                    Result.success(Unit)
                },
                onFailure = {
                    PassLogger.w(TAG, "Pending events error")
                    PassLogger.w(TAG, it)
                    Result.failure(it)
                }
            )

    private suspend fun performRefreshInvites(): Result<Unit> =
        runCatching { refreshInvites() }
            .fold(
                onSuccess = {
                    PassLogger.i(TAG, "Refresh invites finished")
                    Result.success(Unit)
                },
                onFailure = {
                    PassLogger.i(TAG, "Refresh invites error")
                    PassLogger.w(TAG, it)
                    Result.failure(it)
                }
            )

    companion object {
        private const val TAG = "PerformSyncImpl"
    }
}

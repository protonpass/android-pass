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
        val pendingEvents = async { performPendingEvents() }
        val refreshInvites = async { performRefreshInvites() }
        val res = awaitAll(pendingEvents, refreshInvites)

        val error = res.firstOrNull { it.isFailure }
        if (error != null) {
            val exception = error.exceptionOrNull()
            return@withContext if (exception != null) {
                Result.failure(exception)
            } else {
                Result.failure(RuntimeException("Error performing sync"))
            }
        }
        Result.success(Unit)
    }


    private suspend fun performPendingEvents(): Result<Unit> {
        return runCatching {
            applyPendingEvents()
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = {
                PassLogger.w(TAG, it, "Apply pending events error")
                Result.failure(it)
            }
        )
    }

    private suspend fun performRefreshInvites(): Result<Unit> {
        return runCatching {
            refreshInvites()
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = {
                PassLogger.w(TAG, it, "Refresh invites")
                Result.failure(it)
            }
        )
    }

    companion object {
        private const val TAG = "PerformSyncImpl"
    }
}

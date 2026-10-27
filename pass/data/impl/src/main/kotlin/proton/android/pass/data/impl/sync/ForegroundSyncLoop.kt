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

package proton.android.pass.data.impl.sync

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.Account
import proton.android.pass.log.api.LogAccountContext
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.TimeSource

@Singleton
class ForegroundSyncLoop(
    private val performSync: PerformSync,
    private val timeSource: TimeSource.WithComparableMarks
) {

    private val states = ConcurrentHashMap<UserId, UserSyncState>()

    @Inject
    constructor(performSync: PerformSync) : this(performSync, TimeSource.Monotonic)

    suspend fun run(accounts: List<Account>, baseInterval: Duration) {
        val userIds = accounts.map { it.userId }.toSet()
        states.keys.retainAll(userIds)
        coroutineScope {
            userIds.forEach { userId ->
                launch { runForUser(userId, baseInterval) }
            }
        }
    }

    private suspend fun runForUser(userId: UserId, baseInterval: Duration) {
        val state = states.getOrPut(userId) { UserSyncState() }
        while (coroutineContext.isActive) {
            state.retryAt?.let { delay(-it.elapsedNow()) }

            withContext(LogAccountContext(userId)) {
                safeRunCatching {
                    performSync(userId = userId, trigger = "foreground_manager").getOrThrow()
                }
                    .onSuccess {
                        PassLogger.i(TAG, "Sync finished")
                        state.backoff.onSuccess()
                    }
                    .onFailure { error ->
                        PassLogger.w(TAG, "Error in performSync")
                        PassLogger.w(TAG, error)
                        state.backoff.onFailure()
                    }
            }

            val nextDelay = state.backoff.nextDelay(baseInterval)
            state.retryAt = if (state.backoff.hasFailed) timeSource.markNow() + nextDelay else null
            delay(nextDelay)
        }
    }

    private class UserSyncState {
        val backoff = SyncBackoff()

        @Volatile
        var retryAt: ComparableTimeMark? = null
    }

    private companion object {

        private const val TAG = "ForegroundSyncLoop"

    }

}

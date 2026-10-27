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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakePerformSync
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.Account
import proton.android.pass.domain.account.AccountState
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ForegroundSyncLoopTest {

    private val performSync = FakePerformSync()

    private fun TestScope.loop() = ForegroundSyncLoop(performSync, testScheduler.timeSource)

    private fun syncCount(userId: UserId) = performSync.invokedUserIds.count { it == userId }

    @Test
    fun `backoff survives the loop being cancelled and restarted`() = runTest {
        performSync.setResult(Result.failure(IllegalStateException()))
        val loop = loop()
        val accounts = listOf(account(USER_A))

        val first = launch { loop.run(accounts, BASE) }
        runCurrent()
        advanceTimeBy(BASE * 2 + 1.seconds)
        first.cancelAndJoin()
        assertThat(syncCount(USER_A)).isEqualTo(2)

        val second = launch { loop.run(accounts, BASE) }
        runCurrent()
        assertThat(syncCount(USER_A)).isEqualTo(2)

        advanceTimeBy(BASE * 4)
        assertThat(syncCount(USER_A)).isEqualTo(3)
        second.cancelAndJoin()
    }

    @Test
    fun `restart after a success syncs immediately`() = runTest {
        val loop = loop()
        val accounts = listOf(account(USER_A))

        val first = launch { loop.run(accounts, BASE) }
        runCurrent()
        first.cancelAndJoin()

        val second = launch { loop.run(accounts, BASE) }
        runCurrent()

        assertThat(syncCount(USER_A)).isEqualTo(2)
        second.cancelAndJoin()
    }

    @Test
    fun `a failing account does not slow down a healthy one`() = runTest {
        performSync.setResultFor(USER_B, Result.failure(IllegalStateException()))
        val loop = loop()

        val job = launch { loop.run(listOf(account(USER_A), account(USER_B)), BASE) }
        runCurrent()
        advanceTimeBy(BASE * 4 + 1.seconds)

        assertThat(syncCount(USER_A)).isEqualTo(5)
        assertThat(syncCount(USER_B)).isEqualTo(2)
        job.cancelAndJoin()
    }

    @Test
    fun `failing account keeps its own backoff when another account is added`() = runTest {
        performSync.setResultFor(USER_A, Result.failure(IllegalStateException()))
        val loop = loop()

        val first = launch { loop.run(listOf(account(USER_A)), BASE) }
        runCurrent()
        advanceTimeBy(BASE * 2 + 1.seconds)
        first.cancelAndJoin()
        val syncsBeforeSwitch = syncCount(USER_A)

        val second = launch { loop.run(listOf(account(USER_A), account(USER_B)), BASE) }
        runCurrent()

        assertThat(syncCount(USER_A)).isEqualTo(syncsBeforeSwitch)
        assertThat(syncCount(USER_B)).isEqualTo(1)
        second.cancelAndJoin()
    }

    private fun account(userId: UserId) = Account(
        userId = userId,
        username = userId.id,
        state = AccountState.Ready,
        isStepNeeded = false
    )

    private companion object {
        private val BASE = 30.seconds
        private val USER_A = UserId("user-a")
        private val USER_B = UserId("user-b")
    }
}

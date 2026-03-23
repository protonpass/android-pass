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

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.data.fakes.usecases.FakePerformSync

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeAccountManager: FakeAccountManager
    private lateinit var fakePerformSync: FakePerformSync

    @Before
    fun setup() {
        fakeAccountManager = FakeAccountManager()
        fakePerformSync = FakePerformSync()
    }

    @Test
    fun returnsSuccessWhenAllAccountsSyncSuccessfully() = runTest {
        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsFailureWhenSyncFails() = runTest {
        fakePerformSync.setResult(Result.failure(RuntimeException("sync error")))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun syncsEachReadyAccount() = runTest {
        val userId1 = UserId("user-1")
        val userId2 = UserId("user-2")
        fakeAccountManager.setAccounts(
            listOf(
                FakeAccountManager.createAccount(userId1),
                FakeAccountManager.createAccount(userId2)
            )
        )

        buildWorker().doWork()

        assertThat(fakePerformSync.invokedUserIds).containsExactly(userId1, userId2)
    }

    @Test
    fun returnsSuccessWhenNoAccountsAreReady() = runTest {
        fakeAccountManager.setAccounts(emptyList())

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakePerformSync.invokedUserIds).isEmpty()
    }

    @Test
    fun syncsDefaultAccountWithCorrectUserId() = runTest {
        buildWorker().doWork()

        assertThat(fakePerformSync.invokedUserIds)
            .containsExactly(UserId(FakeAccountManager.USER_ID))
    }

    private fun buildWorker(): SyncWorker =
        TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = SyncWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    performSync = fakePerformSync,
                    accountManager = fakeAccountManager
                )
            })
            .build()
}

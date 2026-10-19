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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.fakes.usecases.FakeObserveReadyAccounts
import proton.android.pass.data.fakes.usecases.sync.FakeCheckFolderForceSync
import proton.android.pass.domain.UserId
import proton.android.pass.test.domain.AccountTestFactory

@RunWith(AndroidJUnit4::class)
class FolderRepairWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var checkFolderForceSync: FakeCheckFolderForceSync
    private lateinit var observeReadyAccounts: FakeObserveReadyAccounts

    @Before
    fun setup() {
        checkFolderForceSync = FakeCheckFolderForceSync()
        observeReadyAccounts = FakeObserveReadyAccounts()
    }

    @Test
    fun checksEveryReadyAccountInBackgroundMode() = runTest {
        val first = UserId("first")
        val second = UserId("second")
        observeReadyAccounts.emitAccounts(
            listOf(
                AccountTestFactory.create(first),
                AccountTestFactory.create(second)
            )
        )

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(checkFolderForceSync.detailedInvocations).containsExactly(
            FakeCheckFolderForceSync.Invocation(first, SyncMode.Background),
            FakeCheckFolderForceSync.Invocation(second, SyncMode.Background)
        )
    }

    @Test
    fun succeedsWhenThereAreNoAccounts() = runTest {
        observeReadyAccounts.emitAccounts(emptyList())

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(checkFolderForceSync.invocations).isEmpty()
    }

    private fun buildWorker(): FolderRepairWorker =
        TestListenableWorkerBuilder<FolderRepairWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = FolderRepairWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    checkFolderForceSync = checkFolderForceSync,
                    observeReadyAccounts = observeReadyAccounts
                )
            })
            .build()
}

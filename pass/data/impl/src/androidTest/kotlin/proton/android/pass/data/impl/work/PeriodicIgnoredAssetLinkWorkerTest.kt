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

package proton.android.pass.data.impl.work

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
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository

@RunWith(AndroidJUnit4::class)
class PeriodicIgnoredAssetLinkWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeAssetLinkRepository: FakeAssetLinkRepository

    @Before
    fun setup() {
        fakeAssetLinkRepository = FakeAssetLinkRepository()
    }

    @Test
    fun returnsSuccessOnNormalExecution() = runTest {
        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun callsRefreshIgnored() = runTest {
        buildWorker().doWork()

        assertThat(fakeAssetLinkRepository.refreshIgnoredInvocations).isEqualTo(1)
    }

    @Test
    fun returnsFailureWhenRepositoryThrows() = runTest {
        fakeAssetLinkRepository.setRefreshIgnoredException(RuntimeException("network error"))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun buildWorker(): PeriodicIgnoredAssetLinkWorker =
        TestListenableWorkerBuilder<PeriodicIgnoredAssetLinkWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = PeriodicIgnoredAssetLinkWorker(
                    appContext = appContext,
                    workerParameters = workerParameters,
                    assetLinkRepository = fakeAssetLinkRepository
                )
            })
            .build()
}

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

package proton.android.pass.data.impl.migration

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

@RunWith(AndroidJUnit4::class)
class DataMigrationWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeDataMigrator: FakeDataMigrator

    @Before
    fun setup() {
        fakeDataMigrator = FakeDataMigrator()
    }

    @Test
    fun returnsSuccessWhenMigrationSucceeds() = runTest {
        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsFailureWhenMigrationFails() = runTest {
        fakeDataMigrator.setResult(Result.failure(RuntimeException("migration error")))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun invokesMigrator() = runTest {
        buildWorker().doWork()

        assertThat(fakeDataMigrator.runInvocations).isEqualTo(1)
    }

    private fun buildWorker(): DataMigrationWorker =
        TestListenableWorkerBuilder<DataMigrationWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = DataMigrationWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    dataMigrator = fakeDataMigrator
                )
            })
            .build()
}

private class FakeDataMigrator : DataMigrator {

    var runInvocations = 0
        private set
    private var result: Result<Unit> = Result.success(Unit)

    fun setResult(value: Result<Unit>) {
        result = value
    }

    override suspend fun areMigrationsNeeded(): Boolean = result.isSuccess

    override suspend fun run(): Result<Unit> {
        runInvocations++
        return result
    }
}

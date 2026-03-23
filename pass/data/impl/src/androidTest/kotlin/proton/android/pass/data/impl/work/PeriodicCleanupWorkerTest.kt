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
import proton.android.pass.files.api.DirectoryType

@RunWith(AndroidJUnit4::class)
class PeriodicCleanupWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeDirectoryCleaner: FakeDirectoryCleaner

    @Before
    fun setup() {
        fakeDirectoryCleaner = FakeDirectoryCleaner()
    }

    @Test
    fun returnsSuccessWhenAllDirectoriesCleaned() = runTest {
        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun cleansAllThreeDirectories() = runTest {
        buildWorker().doWork()

        assertThat(fakeDirectoryCleaner.deletedDirs).containsExactlyElementsIn(
            listOf(
                DirectoryType.CameraTemp,
                DirectoryType.ShareTemp,
                DirectoryType.OrphanedAttachments
            )
        )
    }

    @Test
    fun returnsFailureWhenDirectoryCleanerThrows() = runTest {
        fakeDirectoryCleaner.setResult(Result.failure(RuntimeException("disk error")))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun buildWorker(): PeriodicCleanupWorker =
        TestListenableWorkerBuilder<PeriodicCleanupWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = PeriodicCleanupWorker(
                    appContext = appContext,
                    workerParameters = workerParameters,
                    directoryCleaner = fakeDirectoryCleaner
                )
            })
            .build()
}

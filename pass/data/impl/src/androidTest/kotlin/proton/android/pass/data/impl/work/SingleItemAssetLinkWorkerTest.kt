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
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SingleItemAssetLinkWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeUpdateAssetLink: FakeUpdateAssetLink

    @Before
    fun setup() {
        fakeUpdateAssetLink = FakeUpdateAssetLink()
    }

    @Test
    fun returnsSuccessWithWebsites() = runTest {
        val result = buildWorker(websites = setOf("example.com")).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsFailureWhenWebsitesKeyMissing() = runTest {
        val result = buildWorker(includeWebsites = false).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        assertThat(fakeUpdateAssetLink.invocations).isEmpty()
    }

    @Test
    fun doesNotCallUpdateAssetLinkWithEmptyWebsites() = runTest {
        buildWorker(websites = emptySet()).doWork()

        assertThat(fakeUpdateAssetLink.invocations).isEmpty()
    }

    @Test
    fun passesWebsitesToUseCase() = runTest {
        val websites = setOf("example.com", "proton.me")

        buildWorker(websites = websites).doWork()

        assertThat(fakeUpdateAssetLink.invocations).hasSize(1)
        assertThat(fakeUpdateAssetLink.invocations.first()).containsExactlyElementsIn(websites)
    }

    @Test
    fun returnsFailureWhenUpdateAssetLinkThrows() = runTest {
        fakeUpdateAssetLink.setResult(Result.failure(RuntimeException("network error")))

        val result = buildWorker(websites = setOf("example.com")).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun buildWorker(
        websites: Set<String> = setOf("example.com"),
        includeWebsites: Boolean = true
    ): SingleItemAssetLinkWorker {
        val inputData = if (includeWebsites) {
            workDataOf("WEBSITES" to websites.toTypedArray())
        } else {
            workDataOf()
        }
        return TestListenableWorkerBuilder<SingleItemAssetLinkWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = SingleItemAssetLinkWorker(
                    appContext = appContext,
                    workerParameters = workerParameters,
                    updateAssetLink = fakeUpdateAssetLink
                )
            })
            .build()
    }
}

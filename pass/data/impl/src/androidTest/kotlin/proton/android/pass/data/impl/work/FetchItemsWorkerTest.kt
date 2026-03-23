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
import androidx.work.Data
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
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.fakes.usecases.sync.FakeForceSyncItems
import proton.android.pass.domain.ShareId

@RunWith(AndroidJUnit4::class)
class FetchItemsWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeForceSyncItems: FakeForceSyncItems

    @Before
    fun setup() {
        fakeForceSyncItems = FakeForceSyncItems()
    }

    @Test
    fun returnsSuccessOnSuccess() = runTest {
        fakeForceSyncItems.setResult(ForceSyncResult.Success)

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsSuccessOnPartialSuccess() = runTest {
        fakeForceSyncItems.setResult(ForceSyncResult.PartialSuccess)

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsRetryOnError() = runTest {
        fakeForceSyncItems.setResult(ForceSyncResult.Error)

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun returnsFailureWhenUserIdMissing() = runTest {
        val result = buildWorker(includeUserId = false).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        assertThat(fakeForceSyncItems.invocations).isEmpty()
    }

    @Test
    fun returnsFailureWhenFetchSourceMissing() = runTest {
        val result = buildWorker(includeFetchSource = false).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        assertThat(fakeForceSyncItems.invocations).isEmpty()
    }

    @Test
    fun passesCorrectParametersToUseCase() = runTest {
        val shareId = ShareId("test-share-id")

        buildWorker(shareIds = setOf(shareId)).doWork()

        assertThat(fakeForceSyncItems.invocations).hasSize(1)
        assertThat(fakeForceSyncItems.invocations.first().userId).isEqualTo(USER_ID)
        assertThat(fakeForceSyncItems.invocations.first().shareIds).containsExactly(shareId)
    }

    @Test
    fun passesWarningFlagsToUseCase() = runTest {
        buildWorker(
            hasInactiveShares = true,
            hasInvalidGroupShares = true,
            hasInvalidAddressShares = true
        ).doWork()

        assertThat(fakeForceSyncItems.invocations).hasSize(1)
        assertThat(fakeForceSyncItems.invocations.first().hasInactiveShares).isTrue()
        assertThat(fakeForceSyncItems.invocations.first().hasInvalidGroupShares).isTrue()
        assertThat(fakeForceSyncItems.invocations.first().hasInvalidAddressShares).isTrue()
    }

    private fun buildWorker(
        userId: UserId = USER_ID,
        shareIds: Set<ShareId> = emptySet(),
        fetchSource: FetchItemsWorker.FetchSource = FetchItemsWorker.FetchSource.ForceSync,
        hasInactiveShares: Boolean = false,
        hasInvalidGroupShares: Boolean = false,
        hasInvalidAddressShares: Boolean = false,
        includeUserId: Boolean = true,
        includeFetchSource: Boolean = true
    ): FetchItemsWorker {
        val inputData = Data.Builder()
            .apply {
                if (includeUserId) putString("user_id", userId.id)
                if (includeFetchSource) putString("fetch_source", fetchSource.name)
                putStringArray("share_ids", shareIds.map { it.id }.toTypedArray())
                putBoolean("inactive_shares", hasInactiveShares)
                putBoolean("invalid_group_shares", hasInvalidGroupShares)
                putBoolean("invalid_address_shares", hasInvalidAddressShares)
            }
            .build()
        return TestListenableWorkerBuilder<FetchItemsWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = FetchItemsWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    forceSyncItems = fakeForceSyncItems
                )
            })
            .build()
    }

    companion object {
        private val USER_ID = UserId("test-user-id")
    }
}

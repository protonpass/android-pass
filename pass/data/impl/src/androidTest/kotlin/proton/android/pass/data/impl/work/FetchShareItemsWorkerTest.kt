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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.repositories.ItemRevisionTestFactory
import proton.android.pass.data.fakes.usecases.folders.FakeRefreshFolders
import proton.android.pass.data.impl.repositories.FetchShareItemsStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepositoryImpl
import proton.android.pass.domain.ShareId

@RunWith(AndroidJUnit4::class)
class FetchShareItemsWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeItemRepository: FakeItemRepository
    private lateinit var fakeRefreshFolders: FakeRefreshFolders
    private lateinit var fakeStatusRepository: FetchShareItemsStatusRepositoryImpl

    @Before
    fun setup() {
        fakeItemRepository = FakeItemRepository()
        fakeRefreshFolders = FakeRefreshFolders()
        fakeStatusRepository = FetchShareItemsStatusRepositoryImpl()
    }

    @Test
    fun returnsSuccessWhenAllItemsDecryptSuccessfully() = runTest {
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(3) { itemRevision(it) })

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakeStatusRepository.observe(SHARE_ID).first())
            .isEqualTo(FetchShareItemsStatus.Done(3))
    }

    @Test
    fun emitsDoneWithInsertedCountWhenSomeItemsFailToDecrypt() = runTest {
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(5) { itemRevision(it) })
        fakeItemRepository.setShareItemsInsertedCount = 3
        fakeItemRepository.setShareItemsFailedShareIds = setOf(SHARE_ID)

        buildWorker().doWork()

        assertThat(fakeStatusRepository.observe(SHARE_ID).first())
            .isEqualTo(FetchShareItemsStatus.Done(3))
    }

    @Test
    fun returnsFailureWhenShareHasCryptoFailures() = runTest {
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(5) { itemRevision(it) })
        fakeItemRepository.setShareItemsInsertedCount = 3
        fakeItemRepository.setShareItemsFailedShareIds = setOf(SHARE_ID)

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun returnsSuccessWhenCryptoFailureIsForDifferentShare() = runTest {
        val otherShareId = ShareId("other-share-id")
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(3) { itemRevision(it) })
        fakeItemRepository.setShareItemsFailedShareIds = setOf(otherShareId)

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsRetryOnException() = runTest {
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(3) { itemRevision(it) })
        fakeItemRepository.setSetShareItemsResult(Result.failure(RuntimeException("network error")))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun emitsNotStartedBeforeWork() = runTest {
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(3) { itemRevision(it) })

        assertThat(fakeStatusRepository.observe(SHARE_ID).first())
            .isEqualTo(FetchShareItemsStatus.NotStarted)
    }

    @Test
    fun callsRefreshFoldersWithCorrectArguments() = runTest {
        fakeItemRepository.setDownloadItemsResult(SHARE_ID, List(3) { itemRevision(it) })

        buildWorker().doWork()

        assertThat(fakeRefreshFolders.invocations).hasSize(1)
        assertThat(fakeRefreshFolders.invocations.first().userId).isEqualTo(USER_ID)
        assertThat(fakeRefreshFolders.invocations.first().shareIds).containsExactly(SHARE_ID)
    }

    @Test
    fun returnsFailureWhenUserIdMissing() = runTest {
        val result = buildWorker(includeUserId = false).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun returnsFailureWhenShareIdMissing() = runTest {
        val result = buildWorker(includeShareId = false).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun buildWorker(
        userId: UserId = USER_ID,
        shareId: ShareId = SHARE_ID,
        includeUserId: Boolean = true,
        includeShareId: Boolean = true
    ): FetchShareItemsWorker {
        val inputData = workDataOf(
            *listOfNotNull(
                "user_id".takeIf { includeUserId }?.let { it to userId.id },
                "share_id".takeIf { includeShareId }?.let { it to shareId.id }
            ).toTypedArray()
        )
        return TestListenableWorkerBuilder<FetchShareItemsWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = FetchShareItemsWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    fetchShareItemsStatusRepository = fakeStatusRepository,
                    itemRepository = fakeItemRepository,
                    refreshFolders = fakeRefreshFolders
                )
            })
            .build()
    }

    companion object {
        private val USER_ID = UserId("test-user-id")
        private val SHARE_ID = ShareId("test-share-id")

        private fun itemRevision(index: Int) = ItemRevisionTestFactory.create(itemId = "item-$index")
    }
}

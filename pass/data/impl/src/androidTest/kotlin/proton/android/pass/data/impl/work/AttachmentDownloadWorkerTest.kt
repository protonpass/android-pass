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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.api.usecases.attachments.DownloadAllResult
import proton.android.pass.data.fakes.repositories.FakeAttachmentRepository
import proton.android.pass.data.fakes.usecases.attachments.FakeDownloadAllAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeDownloadSingleAttachment
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserId
import proton.android.pass.domain.attachments.AttachmentId

@RunWith(AndroidJUnit4::class)
class AttachmentDownloadWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeDownloadAllAttachments: FakeDownloadAllAttachments
    private lateinit var fakeDownloadSingleAttachment: FakeDownloadSingleAttachment
    private lateinit var fakeAttachmentRepository: FakeAttachmentRepository
    private lateinit var fakeShareRepository: FakeShareRepository

    @Before
    fun setup() {
        fakeDownloadAllAttachments = FakeDownloadAllAttachments()
        fakeDownloadSingleAttachment = FakeDownloadSingleAttachment()
        fakeAttachmentRepository = FakeAttachmentRepository()
        fakeShareRepository = FakeShareRepository()
    }

    @Test
    fun returnsSuccessWhenSingleAttachmentIsNotDownloaded() = runTest {
        fakeDownloadSingleAttachment.setResult(false)

        val result = buildWorker(singleInput()).doWork()

        assertThat(fakeDownloadSingleAttachment.invocationCount).isEqualTo(1)
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsSuccessWhenSingleAttachmentIsDownloaded() = runTest {
        fakeDownloadSingleAttachment.setResult(true)

        val result = buildWorker(singleInput()).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsSuccessWhenDownloadAllThrows() = runTest {
        fakeDownloadAllAttachments.setException(RuntimeException("error"))

        val result = buildWorker(allInput()).doWork()

        assertThat(fakeDownloadAllAttachments.invocationCount).isEqualTo(1)
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsSuccessWhenDownloadsArePausedDueToStorage() = runTest {
        fakeDownloadAllAttachments.setResult(DownloadAllResult.PausedDueToStorage)

        val result = buildWorker(allInput()).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsSuccessWhenDownloadAllCompletes() = runTest {
        fakeDownloadAllAttachments.setResult(DownloadAllResult.Success)

        val result = buildWorker(allInput()).doWork()

        assertThat(fakeDownloadAllAttachments.invocationCount).isEqualTo(1)
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun returnsFailureWhenUserIdIsMissing() = runTest {
        val input = AttachmentDownloadWorker.getRequestFor(
            userId = USER_ID,
            shareIds = listOf(SHARE_ID),
            wifiOnly = false,
            includeFailed = false
        ).workSpec.input

        val withoutUserId = Data.Builder()
            .putAll(input.keyValueMap.filterKeys { it != USER_ID_KEY })
            .build()

        val result = buildWorker(withoutUserId).doWork()

        assertThat(fakeDownloadAllAttachments.invocationCount).isEqualTo(0)
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun singleInput(): Data = AttachmentDownloadWorker.getRequestForAttachment(
        userId = USER_ID,
        shareId = SHARE_ID,
        itemId = ITEM_ID,
        attachmentId = ATTACHMENT_ID,
        wifiOnly = false
    ).workSpec.input

    private fun allInput(): Data = AttachmentDownloadWorker.getRequestFor(
        userId = USER_ID,
        shareIds = listOf(SHARE_ID),
        wifiOnly = false,
        includeFailed = false
    ).workSpec.input

    private fun buildWorker(input: Data): AttachmentDownloadWorker =
        TestListenableWorkerBuilder<AttachmentDownloadWorker>(context)
            .setInputData(input)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = AttachmentDownloadWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    downloadAllAttachments = fakeDownloadAllAttachments,
                    downloadSingleAttachment = fakeDownloadSingleAttachment,
                    attachmentRepository = fakeAttachmentRepository,
                    shareRepository = fakeShareRepository
                )
            })
            .build()

    private companion object {
        const val USER_ID_KEY = "user_id"
        val USER_ID = UserId("user-id")
        val SHARE_ID = ShareId("share-id")
        val ITEM_ID = ItemId("item-id")
        val ATTACHMENT_ID = AttachmentId("attachment-id")
    }
}

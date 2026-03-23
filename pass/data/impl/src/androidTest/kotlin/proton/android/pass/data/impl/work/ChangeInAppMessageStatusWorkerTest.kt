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
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.fakes.usecases.inappmessages.FakeChangeInAppMessageStatus
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus

@RunWith(AndroidJUnit4::class)
class ChangeInAppMessageStatusWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeChangeInAppMessageStatus: FakeChangeInAppMessageStatus

    @Before
    fun setup() {
        fakeChangeInAppMessageStatus = FakeChangeInAppMessageStatus()
    }

    @Test
    fun returnsSuccessWhenValidInput() = runTest {
        val result = buildWorker(
            userId = USER_ID,
            messageId = MESSAGE_ID,
            status = InAppMessageStatus.Read
        ).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun invokesUseCaseWithCorrectArguments() = runTest {
        buildWorker(
            userId = USER_ID,
            messageId = MESSAGE_ID,
            status = InAppMessageStatus.Dismissed
        ).doWork()

        assertThat(fakeChangeInAppMessageStatus.invocations).hasSize(1)
        assertThat(fakeChangeInAppMessageStatus.invocations.first().userId).isEqualTo(USER_ID)
        assertThat(fakeChangeInAppMessageStatus.invocations.first().messageId).isEqualTo(MESSAGE_ID)
        assertThat(fakeChangeInAppMessageStatus.invocations.first().status)
            .isEqualTo(InAppMessageStatus.Dismissed)
    }

    @Test
    fun returnsFailureWhenUserIdMissing() = runTest {
        val result = buildWorker(
            userId = null,
            messageId = MESSAGE_ID,
            status = InAppMessageStatus.Read
        ).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        assertThat(fakeChangeInAppMessageStatus.invocations).isEmpty()
    }

    @Test
    fun returnsFailureWhenMessageIdMissing() = runTest {
        val result = buildWorker(
            userId = USER_ID,
            messageId = null,
            status = InAppMessageStatus.Read
        ).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        assertThat(fakeChangeInAppMessageStatus.invocations).isEmpty()
    }

    @Test
    fun returnsFailureWhenStatusIsUnknown() = runTest {
        val result = buildWorker(
            userId = USER_ID,
            messageId = MESSAGE_ID,
            status = InAppMessageStatus.Unknown
        ).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
        assertThat(fakeChangeInAppMessageStatus.invocations).isEmpty()
    }

    @Test
    fun returnsFailureWhenUseCaseThrows() = runTest {
        fakeChangeInAppMessageStatus.setResult(Result.failure(RuntimeException("error")))

        val result = buildWorker(
            userId = USER_ID,
            messageId = MESSAGE_ID,
            status = InAppMessageStatus.Read
        ).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    private fun buildWorker(
        userId: UserId?,
        messageId: InAppMessageId?,
        status: InAppMessageStatus
    ): ChangeInAppMessageStatusWorker {
        val inputData = workDataOf(
            *listOfNotNull(
                userId?.let { "USER_ID" to it.id },
                messageId?.let { "IN_APP_MESSAGES" to it.value },
                "IN_APP_MESSAGE_STATUS" to status.value
            ).toTypedArray()
        )
        return TestListenableWorkerBuilder<ChangeInAppMessageStatusWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = ChangeInAppMessageStatusWorker(
                    appContext = appContext,
                    workerParameters = workerParameters,
                    changeInAppMessageStatus = fakeChangeInAppMessageStatus
                )
            })
            .build()
    }

    companion object {
        private val USER_ID = UserId("test-user-id")
        private val MESSAGE_ID = InAppMessageId("test-message-id")
    }
}

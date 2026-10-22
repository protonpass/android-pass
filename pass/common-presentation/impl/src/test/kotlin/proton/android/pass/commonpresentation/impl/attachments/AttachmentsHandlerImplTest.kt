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

package proton.android.pass.commonpresentation.impl.attachments

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import proton.android.pass.biometry.FakeAuthOverrideState
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.fakes.FakeFileHandler
import proton.android.pass.data.api.errors.ProtonCodeException
import proton.android.pass.data.fakes.repositories.FakeDraftAttachmentRepository
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentLinkRepository
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentUpdaterRepository
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveUserAccessData
import proton.android.pass.data.fakes.usecases.attachments.FakeClearAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeDownloadAttachment
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveItemAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveUpdateItemAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeUploadAttachment
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.network.fakes.FakeNetworkMonitor
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import java.net.URI

class AttachmentsHandlerImplTest {

    private lateinit var uploadAttachment: FakeUploadAttachment
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var instance: AttachmentsHandlerImpl

    @Before
    fun setup() {
        uploadAttachment = FakeUploadAttachment()
        snackbarDispatcher = FakeSnackbarDispatcher()
        instance = AttachmentsHandlerImpl(
            draftAttachmentRepository = FakeDraftAttachmentRepository(),
            pendingAttachmentLinkRepository = FakePendingAttachmentLinkRepository(),
            pendingAttachmentUpdaterRepository = FakePendingAttachmentUpdaterRepository(),
            uploadAttachment = uploadAttachment,
            downloadAttachment = FakeDownloadAttachment(),
            clearAttachments = FakeClearAttachments(),
            observeUpdateItemAttachments = FakeObserveUpdateItemAttachments(),
            observeItemAttachments = FakeObserveItemAttachments(),
            fileHandler = FakeFileHandler(),
            snackbarDispatcher = snackbarDispatcher,
            authOverrideState = FakeAuthOverrideState(),
            networkMonitor = FakeNetworkMonitor(),
            observeUserAccessData = FakeObserveUserAccessData(),
            getUserPlan = FakeGetUserPlan()
        )
    }

    @Test
    fun `too many files proton code shows the too many files snackbar`() = runTest {
        uploadAttachment.setResult(Result.failure(TestProtonCodeException(TOO_MANY_FILES_CODE)))

        instance.uploadNewAttachment(FILE_METADATA)

        assertThat(lastMessage()).isEqualTo(AttachmentSnackbarMessages.AttachmentTooManyFilesError)
    }

    @Test
    fun `other proton codes show the generic upload snackbar`() = runTest {
        uploadAttachment.setResult(Result.failure(TestProtonCodeException(OTHER_CODE)))

        instance.uploadNewAttachment(FILE_METADATA)

        assertThat(lastMessage()).isEqualTo(AttachmentSnackbarMessages.UploadAttachmentsError)
    }

    @Test
    fun `errors without a proton code show the generic upload snackbar`() = runTest {
        uploadAttachment.setResult(Result.failure(TestProtonCodeException(null)))

        instance.uploadNewAttachment(FILE_METADATA)

        assertThat(lastMessage()).isEqualTo(AttachmentSnackbarMessages.UploadAttachmentsError)
    }

    private suspend fun lastMessage(): SnackbarMessage? = (snackbarDispatcher.snackbarMessage.first() as? Some)?.value

    private class TestProtonCodeException(override val protonCode: Int?) : Exception("error"), ProtonCodeException

    private companion object {
        const val TOO_MANY_FILES_CODE = 2028
        const val OTHER_CODE = 2029
        val FILE_METADATA = FileMetadata(
            uri = URI("file:///tmp/file.txt"),
            name = "file.txt",
            size = 1L,
            mimeType = "text/plain",
            attachmentType = AttachmentType.Unknown,
            createTime = Instant.fromEpochSeconds(0)
        )
    }
}

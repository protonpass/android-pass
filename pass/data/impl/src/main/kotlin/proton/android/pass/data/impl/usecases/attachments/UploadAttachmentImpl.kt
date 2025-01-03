/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.usecases.attachments

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.domain.attachments.DraftAttachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadAttachmentImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val attachmentRepository: AttachmentRepository,
    private val draftAttachmentRepository: DraftAttachmentRepository,
    private val telemetryManager: TelemetryManager
) : UploadAttachment {

    override suspend fun invoke(metadata: FileMetadata) {
        runCatching {
            val draftAttachment = DraftAttachment.Loading(metadata)
            draftAttachmentRepository.update(draftAttachment)
            val userId = accountManager.getPrimaryUserId().firstOrNull()
                ?: throw UserIdNotAvailableError()
            val pendingAttachmentId = attachmentRepository.createPendingAttachment(
                userId = userId,
                metadata = metadata
            )
            runCatching {
                attachmentRepository.uploadPendingAttachment(
                    userId = userId,
                    pendingAttachmentId = pendingAttachmentId,
                    uri = metadata.uri
                )
                telemetryManager.sendEvent(FileUploaded(metadata.mimeType))
            }.onSuccess {
                val success = DraftAttachment.Success(
                    metadata = metadata,
                    pendingAttachmentId = pendingAttachmentId
                )
                draftAttachmentRepository.update(success)
            }.onFailure { throw it }
        }.onFailure {
            val error = DraftAttachment.Error(
                metadata = metadata,
                errorMessage = it.message ?: "Unknown error"
            )
            draftAttachmentRepository.update(error)
            throw it
        }
    }
}

data class FileUploaded(
    val mimeType: String
) : DeferredTelemetryEvent("pass_file_attachment.file_uploaded") {
    override fun dimensions(): Map<String, String> = mapOf("mimeType" to mimeType)
}

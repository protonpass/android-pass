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

package proton.android.pass.data.impl.fakes

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.data.impl.responses.attachments.FileApiModel
import proton.android.pass.data.impl.responses.attachments.FileIdApiModel
import proton.android.pass.data.impl.responses.attachments.FileResult
import proton.android.pass.data.impl.responses.attachments.FilesApiModel
import proton.android.pass.data.impl.responses.attachments.PendingFileResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.domain.attachments.PendingAttachmentId

class FakeRemoteAttachmentsDataSource : RemoteAttachmentsDataSource {

    var createPendingFileResult: PendingFileResponse = PendingFileResponse(
        code = 1000,
        file = FileIdApiModel(fileID = "fake-file-id")
    )
    var createPendingFileInvocations: Int = 0
    var shouldThrowOnCreatePendingFile: Throwable? = null

    var updatePendingFileResult: String = "fake-pending-file-id"
    var updatePendingFileInvocations: Int = 0
    var shouldThrowOnUpdatePendingFile: Throwable? = null

    var uploadPendingFileInvocations: Int = 0
    var shouldThrowOnUploadPendingFile: Throwable? = null

    var linkPendingFilesInvocations: Int = 0
    var shouldThrowOnLinkPendingFiles: Throwable? = null

    var restoreOldFileResult: FileResult? = null
    var restoreOldFileInvocations: Int = 0
    var shouldThrowOnRestoreOldFile: Throwable? = null

    var updateFileMetadataResult: FileApiModel? = null
    var updateFileMetadataInvocations: Int = 0
    var shouldThrowOnUpdateFileMetadata: Throwable? = null

    var retrieveActiveFilesResult: FilesApiModel = FilesApiModel(files = emptyList(), total = 0, lastId = null)
    var retrieveActiveFilesInvocations: Int = 0
    var shouldThrowOnRetrieveActiveFiles: Throwable? = null

    var retrieveFilesForAllRevisionsResult: FilesApiModel = FilesApiModel(files = emptyList(), total = 0, lastId = null)
    var retrieveFilesForAllRevisionsInvocations: Int = 0
    var shouldThrowOnRetrieveFilesForAllRev: Throwable? = null

    var chunksToReturn: Map<ChunkId, EncryptedByteArray> = emptyMap()
    var downloadChunkInvocations: Int = 0
    var shouldThrowOnDownload: Throwable? = null

    fun reset() {
        createPendingFileInvocations = 0
        shouldThrowOnCreatePendingFile = null
        updatePendingFileInvocations = 0
        shouldThrowOnUpdatePendingFile = null
        uploadPendingFileInvocations = 0
        shouldThrowOnUploadPendingFile = null
        linkPendingFilesInvocations = 0
        shouldThrowOnLinkPendingFiles = null
        restoreOldFileInvocations = 0
        shouldThrowOnRestoreOldFile = null
        updateFileMetadataInvocations = 0
        shouldThrowOnUpdateFileMetadata = null
        retrieveActiveFilesInvocations = 0
        shouldThrowOnRetrieveActiveFiles = null
        retrieveFilesForAllRevisionsInvocations = 0
        shouldThrowOnRetrieveFilesForAllRev = null
        chunksToReturn = emptyMap()
        downloadChunkInvocations = 0
        shouldThrowOnDownload = null
    }

    override suspend fun createPendingFile(
        userId: UserId,
        metadata: EncryptedString,
        chunkCount: Int,
        encryptionVersion: Int
    ): PendingFileResponse {
        createPendingFileInvocations++
        shouldThrowOnCreatePendingFile?.let { throw it }
        return createPendingFileResult
    }

    override suspend fun updatePendingFile(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        metadata: EncryptedString
    ): String {
        updatePendingFileInvocations++
        shouldThrowOnUpdatePendingFile?.let { throw it }
        return updatePendingFileResult
    }

    override suspend fun uploadPendingFile(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        chunkIndex: Int,
        encryptedByteArray: EncryptedByteArray
    ) {
        uploadPendingFileInvocations++
        shouldThrowOnUploadPendingFile?.let { throw it }
    }

    override suspend fun linkPendingFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        filesToAdd: Map<PendingAttachmentId, EncryptedString>,
        filesToRemove: Set<AttachmentId>
    ) {
        linkPendingFilesInvocations++
        shouldThrowOnLinkPendingFiles?.let { throw it }
    }

    override suspend fun restoreOldFile(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        itemKeyRotation: Int,
        fileKey: EncryptedString
    ): FileResult {
        restoreOldFileInvocations++
        shouldThrowOnRestoreOldFile?.let { throw it }
        return restoreOldFileResult ?: error("restoreOldFileResult not set")
    }

    override suspend fun updateFileMetadata(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        metadata: EncryptedString
    ): FileApiModel {
        updateFileMetadataInvocations++
        shouldThrowOnUpdateFileMetadata?.let { throw it }
        return updateFileMetadataResult ?: error("updateFileMetadataResult not set")
    }

    override suspend fun retrieveActiveFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        lastToken: String?
    ): FilesApiModel {
        retrieveActiveFilesInvocations++
        shouldThrowOnRetrieveActiveFiles?.let { throw it }
        return retrieveActiveFilesResult
    }

    override suspend fun retrieveFilesForAllRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        lastToken: String?
    ): FilesApiModel {
        retrieveFilesForAllRevisionsInvocations++
        shouldThrowOnRetrieveFilesForAllRev?.let { throw it }
        return retrieveFilesForAllRevisionsResult
    }

    override suspend fun downloadChunk(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        chunkId: ChunkId
    ): EncryptedByteArray {
        downloadChunkInvocations++
        shouldThrowOnDownload?.let { throw it }
        return chunksToReturn[chunkId] ?: EncryptedByteArray(byteArrayOf())
    }
}

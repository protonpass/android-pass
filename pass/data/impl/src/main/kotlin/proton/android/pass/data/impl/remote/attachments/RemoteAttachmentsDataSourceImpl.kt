/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.remote.attachments

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.attachments.CreatePendingFileRequest
import proton.android.pass.data.impl.requests.attachments.LinkPendingFileRequest
import proton.android.pass.data.impl.requests.attachments.LinkPendingFilesRequest
import proton.android.pass.data.impl.responses.attachments.FilesApiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.ChunkId
import javax.inject.Inject

class RemoteAttachmentsDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteAttachmentsDataSource {

    override suspend fun createPendingFile(userId: UserId, metadata: EncryptedString): String =
        api.get<PasswordManagerApi>(userId)
            .invoke { createPendingFile(CreatePendingFileRequest(metadata)) }
            .valueOrThrow
            .file
            .fileID

    override suspend fun uploadPendingFile(
        userId: UserId,
        attachmentId: AttachmentId,
        chunkIndex: Int,
        encryptedByteArray: EncryptedByteArray
    ) {
        val chunkIndexBody = "$chunkIndex".toRequestBody("text/plain".toMediaTypeOrNull())
        val chunkDataPart = MultipartBody.Part.createFormData(
            name = "ChunkData",
            filename = "no-op", // not used by the backend
            body = encryptedByteArray.array.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        )
        api.get<PasswordManagerApi>(userId)
            .invoke { uploadChunk(attachmentId.id, chunkIndexBody, chunkDataPart) }
            .valueOrThrow
    }

    override suspend fun linkPendingFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        filesToAdd: Map<AttachmentId, EncryptedString>,
        filesToRemove: Set<AttachmentId>
    ) {
        val pendingFilesRequest = LinkPendingFilesRequest(
            revision = revision,
            filesToAdd = filesToAdd.map { (id, key) ->
                LinkPendingFileRequest(fileID = id.id, fileKey = key)
            },
            filesToRemove = filesToRemove.map { it.id }
        )
        api.get<PasswordManagerApi>(userId)
            .invoke { linkPendingFiles(shareId.id, itemId.id, pendingFilesRequest) }
            .valueOrThrow
    }

    override suspend fun retrieveAllFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): FilesApiModel = api.get<PasswordManagerApi>(userId)
        .invoke { retrieveActiveFiles(shareId.id, itemId.id) }
        .valueOrThrow
        .filesData

    override suspend fun downloadChunk(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        chunkId: ChunkId
    ): EncryptedByteArray = api.get<PasswordManagerApi>(userId)
        .invoke { downloadChunk(shareId.id, itemId.id, attachmentId.id, chunkId.id) }
        .valueOrThrow
        .body()
        ?.bytes()
        ?.let(::EncryptedByteArray)
        ?: throw IllegalStateException("Chunk download failed for attachmentId: $attachmentId, chunkId: $chunkId")
}

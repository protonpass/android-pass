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

import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.attachments.CreatePendingFileRequest
import proton.android.pass.data.impl.requests.attachments.LinkPendingFileRequest
import proton.android.pass.data.impl.requests.attachments.LinkPendingFilesRequest
import proton.android.pass.data.impl.responses.attachments.FilesDataResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentKey
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
        byteArray: ByteArray
    ) {
        val chunkIndex = "0".toRequestBody("text/plain".toMediaTypeOrNull())
        val chunkData = byteArray.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        api.get<PasswordManagerApi>(userId)
            .invoke { uploadChunk(attachmentId.id, chunkIndex, chunkData) }
            .valueOrThrow
    }

    override suspend fun linkPendingFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Int,
        files: Map<AttachmentId, AttachmentKey>
    ) {
        val pendingFilesRequest = LinkPendingFilesRequest(
            revision = revision,
            files = files.map { (id, key) ->
                LinkPendingFileRequest(fileID = id.id, fileKey = key.value)
            }
        )
        api.get<PasswordManagerApi>(userId)
            .invoke { linkPendingFiles(shareId.id, itemId.id, pendingFilesRequest) }
            .valueOrThrow
    }

    override suspend fun retrieveAllFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): FilesDataResponse = api.get<PasswordManagerApi>(userId)
        .invoke { retrieveAllFiles(shareId.id, itemId.id) }
        .valueOrThrow
        .filesData
}

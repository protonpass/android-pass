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

package proton.android.pass.data.impl.repositories

import FileV1
import fileMetadata
import kotlinx.coroutines.flow.Flow
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonrust.api.FileTypeDetector
import proton.android.pass.commonrust.api.MimeType
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentKey
import proton.android.pass.domain.attachments.PendingAttachmentId
import javax.inject.Inject

class AttachmentRepositoryImpl @Inject constructor(
    private val remote: RemoteAttachmentsDataSource,
    private val fileTypeDetector: FileTypeDetector,
    private val encryptionContextProvider: EncryptionContextProvider
) : AttachmentRepository {

    override suspend fun createPendingAttachment(userId: UserId, attachment: Attachment): PendingAttachmentId {
        val fileKey = EncryptionKey.generate()
        val fileMeta = fileMetadata {
            name = attachment.name
            mimeType = attachment.mimeType
        }
        val fileContents = fileMeta.toByteArray()
        val encryptedMetadata =
            encryptionContextProvider.withEncryptionContextSuspendable(fileKey) {
                encrypt(fileContents, EncryptionTag.FileKey)
            }
        val encodedMetadata = Base64.encodeBase64String(encryptedMetadata.array)
        val id = remote.createPendingFile(userId, encodedMetadata)
        return PendingAttachmentId(id)
    }

    override suspend fun uploadPendingAttachment(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        byteArray: ByteArray
    ) {
        remote.uploadPendingFile(userId, pendingAttachmentId, byteArray)
    }

    override suspend fun linkPendingAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Int,
        attachments: Map<AttachmentId, AttachmentKey>
    ) {
        remote.linkPendingFiles(userId, shareId, itemId, revision, attachments)
    }

    override fun observeAllAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = oneShot {
        val files = remote.retrieveAllFiles(userId, shareId, itemId).files
        return@oneShot encryptionContextProvider.withEncryptionContextSuspendable {
            files.map {
                val decodedMetadata = Base64.decodeBase64(it.metadata)
                val decryptedMetadata =
                    decrypt(EncryptedByteArray(decodedMetadata), EncryptionTag.FileKey)
                val metadata = FileV1.File.parseFrom(decryptedMetadata).metadata
                val fileType = fileTypeDetector.getFileTypeFromMimeType(
                    MimeType(metadata.mimeType)
                )
                it.toDomain(metadata.name, metadata.mimeType, fileType.toDomain())
            }
        }
    }
}

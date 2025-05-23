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
import proton.android.pass.data.impl.responses.attachments.FileApiModel
import proton.android.pass.data.impl.responses.attachments.FileResult
import proton.android.pass.data.impl.responses.attachments.FilesApiModel
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.ChunkId
import proton.android.pass.domain.attachments.PendingAttachmentId

interface RemoteAttachmentsDataSource {

    suspend fun createPendingFile(
        userId: UserId,
        metadata: EncryptedString,
        chunkCount: Int,
        encryptionVersion: Int
    ): String

    suspend fun updatePendingFile(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        metadata: EncryptedString
    ): String

    suspend fun uploadPendingFile(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        chunkIndex: Int,
        encryptedByteArray: EncryptedByteArray
    )

    @Suppress("LongParameterList")
    suspend fun linkPendingFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        filesToAdd: Map<PendingAttachmentId, EncryptedString>,
        filesToRemove: Set<AttachmentId>
    )

    @Suppress("LongParameterList")
    suspend fun restoreOldFile(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        itemKeyRotation: Int,
        fileKey: EncryptedString
    ): FileResult

    suspend fun updateFileMetadata(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        metadata: EncryptedString
    ): FileApiModel

    suspend fun retrieveActiveFiles(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        lastToken: String?
    ): FilesApiModel

    suspend fun retrieveFilesForAllRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        lastToken: String?
    ): FilesApiModel

    suspend fun downloadChunk(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        chunkId: ChunkId
    ): EncryptedByteArray
}

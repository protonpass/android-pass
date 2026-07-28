/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.attachments.PendingAttachmentId
import java.net.URI

@Suppress("TooManyFunctions", "ComplexInterface")
interface AttachmentRepository {

    suspend fun createPendingAttachment(userId: UserId, metadata: FileMetadata): PendingAttachmentId

    suspend fun updatePendingAttachment(
        userId: UserId,
        attachmentId: PendingAttachmentId,
        metadata: FileMetadata
    )

    suspend fun uploadPendingAttachment(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        uri: URI
    )

    @Suppress("LongParameterList")
    suspend fun linkPendingAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        toLink: Map<PendingAttachmentId, PendingAttachmentLinkData>,
        toUnlink: Set<AttachmentId>
    )

    suspend fun updateFileMetadata(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        title: String
    )

    suspend fun restoreOldFile(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    )

    fun observeActiveAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>>

    fun observeAttachmentsForAllRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>>

    suspend fun getAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Attachment

    suspend fun downloadAttachment(userId: UserId, attachment: Attachment): URI

    suspend fun updateDownloadStatus(
        userId: UserId,
        attachment: Attachment,
        status: AttachmentDownloadStatus
    )

    suspend fun resetDownloadingToPending(userId: UserId, shareIds: List<ShareId>)

    suspend fun resetIdleToPending(userId: UserId, shareIds: List<ShareId>)

    suspend fun resetPausedToPending(userId: UserId, shareIds: List<ShareId>)

    suspend fun clearDownloadedAttachments(userId: UserId, shareIds: List<ShareId>)

    suspend fun clearAllDownloadedAttachments(userId: UserId)

    fun observeAllActiveAttachments(userId: UserId, shareIds: List<ShareId>): Flow<List<Attachment>>

    fun observePendingDownloads(
        userId: UserId,
        shareIds: List<ShareId>,
        includeFailed: Boolean
    ): Flow<List<Attachment>>

    fun observeDownloadProgress(userId: UserId, shareIds: List<ShareId>): Flow<Pair<Int, Int>>

    fun observeAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Flow<Attachment?>

    suspend fun refreshAttachmentsForItems(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>
    ): List<Pair<ShareId, ItemId>>
}

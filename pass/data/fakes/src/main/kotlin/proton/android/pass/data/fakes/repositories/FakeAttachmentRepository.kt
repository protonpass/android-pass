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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.PendingAttachmentLinkData
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.attachments.PendingAttachmentId
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAttachmentRepository @Inject constructor() : AttachmentRepository {

    var getAttachmentByIdResult: Result<Attachment> = Result.failure(IllegalStateException("Not set"))

    override suspend fun createPendingAttachment(userId: UserId, metadata: FileMetadata): PendingAttachmentId =
        PendingAttachmentId("")

    override suspend fun updatePendingAttachment(
        userId: UserId,
        attachmentId: PendingAttachmentId,
        metadata: FileMetadata
    ) = Unit

    override suspend fun uploadPendingAttachment(
        userId: UserId,
        pendingAttachmentId: PendingAttachmentId,
        uri: URI
    ) = Unit

    override suspend fun linkPendingAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        revision: Long,
        toLink: Map<PendingAttachmentId, PendingAttachmentLinkData>,
        toUnlink: Set<AttachmentId>
    ) = Unit

    override suspend fun updateFileMetadata(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        title: String
    ) = Unit

    override suspend fun restoreOldFile(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ) = Unit

    override fun observeActiveAttachments(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = flowOf(emptyList())

    override fun observeAttachmentsForAllRevisions(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<Attachment>> = flowOf(emptyList())

    override suspend fun getAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Attachment = getAttachmentByIdResult.getOrThrow()

    override suspend fun downloadAttachment(userId: UserId, attachment: Attachment): URI = URI.create("")

    override suspend fun updateDownloadStatus(
        userId: UserId,
        attachment: Attachment,
        status: AttachmentDownloadStatus
    ) = Unit

    override suspend fun resetDownloadingToPending(userId: UserId, shareIds: List<ShareId>) = Unit

    override suspend fun resetIdleToPending(userId: UserId, shareIds: List<ShareId>) = Unit

    override suspend fun resetPausedToPending(userId: UserId, shareIds: List<ShareId>) = Unit

    override suspend fun clearDownloadedAttachments(userId: UserId, shareIds: List<ShareId>) = Unit

    override suspend fun clearAllDownloadedAttachments(userId: UserId) = Unit

    override fun observeAllActiveAttachments(userId: UserId, shareIds: List<ShareId>): Flow<List<Attachment>> =
        flowOf(emptyList())

    override fun observePendingDownloads(
        userId: UserId,
        shareIds: List<ShareId>,
        includeFailed: Boolean
    ): Flow<List<Attachment>> = flowOf(emptyList())

    override fun observeDownloadProgress(userId: UserId, shareIds: List<ShareId>): Flow<Pair<Int, Int>> = flowOf(0 to 0)

    override fun observeAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Flow<Attachment?> = flowOf(null)

    override suspend fun refreshAttachmentsForItems(
        userId: UserId,
        items: List<Pair<ShareId, ItemId>>
    ): List<Pair<ShareId, ItemId>> = emptyList()
}

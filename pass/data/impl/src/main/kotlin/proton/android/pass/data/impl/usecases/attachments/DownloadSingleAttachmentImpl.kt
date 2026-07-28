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

package proton.android.pass.data.impl.usecases.attachments

import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.usecases.attachments.DownloadAttachment
import proton.android.pass.data.api.usecases.attachments.DownloadSingleAttachment
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadSingleAttachmentImpl @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val downloadAttachment: DownloadAttachment
) : DownloadSingleAttachment {

    override suspend fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Boolean {
        safeRunCatching {
            attachmentRepository.refreshAttachmentsForItems(userId, listOf(shareId to itemId))
        }.onFailure { error ->
            PassLogger.w(TAG, "Failed to refresh attachment metadata for ${attachmentId.id}")
            PassLogger.w(TAG, error)
        }

        val attachment = attachmentRepository
            .observeAttachmentById(userId, shareId, itemId, attachmentId)
            .first()
            ?: return false

        attachmentRepository.updateDownloadStatus(
            userId = userId,
            attachment = attachment,
            status = AttachmentDownloadStatus.Downloading
        )

        return safeRunCatching { downloadAttachment(attachment) }
            .onFailure { error ->
                PassLogger.w(TAG, "Failed to download attachment ${attachmentId.id}")
                PassLogger.w(TAG, error)
                attachmentRepository.updateDownloadStatus(
                    userId = userId,
                    attachment = attachment,
                    status = AttachmentDownloadStatus.Failed
                )
            }
            .isSuccess
    }

    private companion object {

        private const val TAG = "DownloadSingleAttachmentImpl"
    }
}

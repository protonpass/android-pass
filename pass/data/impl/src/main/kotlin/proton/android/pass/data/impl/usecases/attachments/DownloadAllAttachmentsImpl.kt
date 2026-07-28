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

import android.system.ErrnoException
import android.system.OsConstants
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.first
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.impl.util.runConcurrently
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.attachments.DownloadAllAttachments
import proton.android.pass.data.api.usecases.attachments.DownloadAllResult
import proton.android.pass.data.api.usecases.attachments.DownloadAttachment
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadAllAttachmentsImpl @Inject constructor(
    private val attachmentRepository: AttachmentRepository,
    private val downloadAttachment: DownloadAttachment,
    private val localItemDataSource: LocalItemDataSource
) : DownloadAllAttachments {

    override suspend fun invoke(
        userId: UserId,
        shareIds: List<ShareId>,
        includeFailed: Boolean
    ): DownloadAllResult {
        refreshAttachmentMetadata(userId, shareIds)
        attachmentRepository.resetDownloadingToPending(userId, shareIds)
        attachmentRepository.resetIdleToPending(userId, shareIds)
        attachmentRepository.resetPausedToPending(userId, shareIds)

        val pendingAttachments = attachmentRepository
            .observePendingDownloads(userId, shareIds, includeFailed)
            .first()

        if (pendingAttachments.isEmpty()) return DownloadAllResult.Success

        // Note: in-flight downloads that already passed the storageFullDetected check
        // will continue and fail with IOException. With the semaphore limiting concurrency
        // to cores/2 (~2-4), the wasted requests are minimal and fail fast.
        val storageFullDetected = AtomicBoolean(false)

        runConcurrently(
            items = pendingAttachments,
            block = { attachment ->
                if (storageFullDetected.get()) {
                    attachmentRepository.updateDownloadStatus(
                        userId = userId,
                        attachment = attachment,
                        status = AttachmentDownloadStatus.Paused
                    )
                    return@runConcurrently
                }
                attachmentRepository.updateDownloadStatus(userId, attachment, AttachmentDownloadStatus.Downloading)
                safeRunCatching {
                    downloadAttachment(attachment)
                }.onFailure { e ->
                    PassLogger.w(TAG, "Failed to download attachment ${attachment.id.id}")
                    PassLogger.w(TAG, e)
                    attachmentRepository.updateDownloadStatus(
                        userId = userId,
                        attachment = attachment,
                        status = AttachmentDownloadStatus.Failed
                    )
                    if (e.isStorageFullError()) {
                        PassLogger.w(TAG, "Storage full, pausing download queue")
                        storageFullDetected.set(true)
                    }
                }
            }
        )

        return if (storageFullDetected.get()) {
            DownloadAllResult.PausedDueToStorage
        } else {
            DownloadAllResult.Success
        }
    }

    private suspend fun refreshAttachmentMetadata(userId: UserId, shareIds: List<ShareId>) {
        safeRunCatching {
            val itemsWithAttachments = localItemDataSource.observeItems(
                userId = userId,
                shareIds = shareIds,
                itemState = ItemState.Active,
                filter = ItemTypeFilter.All,
                itemFlags = mapOf(ItemFlag.HasAttachments to true)
            ).first()

            val itemPairs = itemsWithAttachments.map {
                ShareId(it.shareId) to proton.android.pass.domain.ItemId(it.id)
            }
            if (itemPairs.isNotEmpty()) {
                attachmentRepository.refreshAttachmentsForItems(userId, itemPairs)
            }
        }.onFailure { e ->
            PassLogger.w(TAG, "Failed to refresh attachment metadata")
            PassLogger.w(TAG, e)
        }
    }

    private fun Throwable.isStorageFullError(): Boolean = generateSequence(this) { it.cause }
        .take(MAX_CAUSE_DEPTH)
        .any { it.isErrnoStorageFull() || it.hasStorageFullMessage() }

    private fun Throwable.isErrnoStorageFull(): Boolean = this is ErrnoException &&
        (errno == OsConstants.ENOSPC || errno == OsConstants.EDQUOT)

    private fun Throwable.hasStorageFullMessage(): Boolean {
        val message = this.message?.lowercase() ?: return false
        return message.contains("no space") ||
            message.contains("enospc") ||
            message.contains("disk quota")
    }

    private companion object {

        private const val TAG = "DownloadAllAttachmentsImpl"
        private const val MAX_CAUSE_DEPTH = 10
    }
}

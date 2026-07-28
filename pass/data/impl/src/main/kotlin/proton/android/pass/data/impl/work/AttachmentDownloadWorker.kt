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

package proton.android.pass.data.impl.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import android.content.pm.ServiceInfo
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.attachments.DownloadAllAttachments
import proton.android.pass.data.api.usecases.attachments.DownloadAllResult
import proton.android.pass.data.api.usecases.attachments.DownloadSingleAttachment
import proton.android.pass.data.impl.R
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.log.api.PassLogger

@HiltWorker
class AttachmentDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val downloadAllAttachments: DownloadAllAttachments,
    private val downloadSingleAttachment: DownloadSingleAttachment,
    private val attachmentRepository: AttachmentRepository,
    private val shareRepository: ShareRepository
) : CoroutineWorker(context, workerParameters) {

    private val notificationId: Int by lazy {
        val ids = inputData.getStringArray(ARG_SHARE_IDS)
        val attachmentId = inputData.getString(ARG_ATTACHMENT_ID)
        val key = attachmentId?.hashCode()
            ?: ids?.takeIf { it.isNotEmpty() }?.toList()?.hashCode()
        if (key == null) {
            DOWNLOAD_NOTIFICATION_ID_BASE
        } else {
            DOWNLOAD_NOTIFICATION_ID_BASE + (key and POSITIVE_INT_MASK)
        }
    }

    @Suppress("ReturnCount")
    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")

        val userId = inputData.getString(ARG_USER_ID)?.let(::UserId) ?: return Result.failure()
        val shareIds = inputData.getStringArray(ARG_SHARE_IDS)
            ?.map(::ShareId)
            ?: return Result.failure()

        setupForeground()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        val itemId = inputData.getString(ARG_ITEM_ID)?.let(::ItemId)
        val attachmentId = inputData.getString(ARG_ATTACHMENT_ID)?.let(::AttachmentId)
        if (itemId != null && attachmentId != null) {
            val shareId = shareIds.firstOrNull() ?: return Result.failure()
            return downloadSingle(notificationManager, userId, shareId, itemId, attachmentId)
        }

        return coroutineScope {
            val progressJob = launchProgressUpdater(notificationManager, userId, shareIds)
            try {
                val includeFailed = inputData.getBoolean(ARG_INCLUDE_FAILED, false)
                val result = safeRunCatching {
                    downloadAllAttachments(userId, shareIds, includeFailed)
                }
                result.fold(
                    onSuccess = ::mapDownloadResult,
                    onFailure = { e ->
                        PassLogger.w(TAG, "$TAG finished with errors")
                        PassLogger.w(TAG, e)
                        Result.failure()
                    }
                )
            } finally {
                progressJob?.cancel()
                notificationManager?.cancel(notificationId)
                withContext(NonCancellable) {
                    runCatching { attachmentRepository.resetDownloadingToPending(userId, shareIds) }
                        .onFailure { e ->
                            PassLogger.w(TAG, "Failed to reset Downloading rows on worker exit")
                            PassLogger.w(TAG, e)
                        }
                }
            }
        }
    }

    private suspend fun downloadSingle(
        notificationManager: NotificationManager?,
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Result = try {
        val isDownloaded = downloadSingleAttachment(userId, shareId, itemId, attachmentId)
        if (isDownloaded) {
            PassLogger.i(TAG, "$TAG finished successfully")
            Result.success()
        } else {
            PassLogger.w(TAG, "$TAG finished with errors")
            Result.failure()
        }
    } finally {
        notificationManager?.cancel(notificationId)
        withContext(NonCancellable) {
            runCatching { attachmentRepository.resetDownloadingToPending(userId, listOf(shareId)) }
                .onFailure { e ->
                    PassLogger.w(TAG, "Failed to reset Downloading row on worker exit")
                    PassLogger.w(TAG, e)
                }
        }
    }

    private suspend fun setupForeground() {
        ensureNotificationChannel()
        safeRunCatching { setForeground(getForegroundInfo()) }
            .onFailure { e ->
                PassLogger.w(TAG, "Could not set foreground info")
                PassLogger.w(TAG, e)
            }
    }

    private fun ensureNotificationChannel() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.createNotificationChannel(
            NotificationChannel(
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.attachment_download_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.attachment_download_channel_description)
            }
        )
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.launchProgressUpdater(
        notificationManager: NotificationManager?,
        userId: UserId,
        shareIds: List<ShareId>
    ) = notificationManager?.takeIf { shareIds.isNotEmpty() }?.let { nm ->
        launch {
            attachmentRepository
                .observeDownloadProgress(userId, shareIds)
                .sample(PROGRESS_UPDATE_INTERVAL_MS)
                .collect { (downloaded, total) ->
                    nm.notify(
                        notificationId,
                        context.downloadWorkNotification(downloaded, total)
                    )
                }
        }
    }

    private fun mapDownloadResult(downloadResult: DownloadAllResult): Result = when (downloadResult) {
        is DownloadAllResult.PausedDueToStorage -> {
            PassLogger.w(TAG, "Downloads paused due to storage full")
            Result.failure()
        }
        is DownloadAllResult.Success -> {
            PassLogger.i(TAG, "$TAG finished successfully")
            Result.success()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        notificationId,
        context.downloadWorkNotification(),
        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
    )

    private fun Context.downloadWorkNotification(downloaded: Int = 0, total: Int = 0): Notification {
        val isIndeterminate = total <= 0

        return NotificationCompat.Builder(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(me.proton.core.notification.R.drawable.ic_proton_brand_proton_pass)
            .setContentTitle(getString(R.string.downloading_attachments))
            .setContentText(
                if (isIndeterminate) null
                else getString(R.string.downloading_attachments_progress, "$downloaded / $total")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total, downloaded, isIndeterminate)
            .build()
    }

    companion object {
        private const val TAG = "AttachmentDownloadWorker"
        private const val ARG_SHARE_IDS = "share_ids"
        private const val ARG_USER_ID = "user_id"
        private const val ARG_ITEM_ID = "item_id"
        private const val ARG_INCLUDE_FAILED = "include_failed"
        private const val ARG_ATTACHMENT_ID = "attachment_id"

        private const val DOWNLOAD_NOTIFICATION_ID_BASE = 100
        private const val POSITIVE_INT_MASK = 0x7FFFFF
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "AttachmentDownloadChannel"
        private const val PROGRESS_UPDATE_INTERVAL_MS = 1_000L

        const val UNIQUE_WORK_PREFIX = "attachment_download_"
        const val SHARED_ITEMS_WORK_PREFIX = "attachment_download_shared_items_"

        fun getRequestFor(
            userId: UserId,
            shareIds: List<ShareId>,
            wifiOnly: Boolean,
            includeFailed: Boolean
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .build()

            return OneTimeWorkRequestBuilder<AttachmentDownloadWorker>()
                .setInputData(
                    workDataOf(
                        ARG_SHARE_IDS to shareIds.map { it.id }.toTypedArray(),
                        ARG_USER_ID to userId.id,
                        ARG_INCLUDE_FAILED to includeFailed
                    )
                )
                .setConstraints(constraints)
                .addTag(TAG)
                .addTag(userTag(userId))
                .build()
        }

        fun getRequestForAttachment(
            userId: UserId,
            shareId: ShareId,
            itemId: ItemId,
            attachmentId: AttachmentId,
            wifiOnly: Boolean
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
                )
                .build()

            return OneTimeWorkRequestBuilder<AttachmentDownloadWorker>()
                .setInputData(
                    workDataOf(
                        ARG_SHARE_IDS to arrayOf(shareId.id),
                        ARG_USER_ID to userId.id,
                        ARG_ITEM_ID to itemId.id,
                        ARG_ATTACHMENT_ID to attachmentId.id
                    )
                )
                .setConstraints(constraints)
                .addTag(TAG)
                .addTag(userTag(userId))
                .build()
        }

        fun uniqueWorkName(shareId: ShareId) = "$UNIQUE_WORK_PREFIX${shareId.id}"

        fun sharedItemsWorkName(userId: UserId): String = "$SHARED_ITEMS_WORK_PREFIX${userId.id}"

        fun userTag(userId: UserId): String = "${TAG}_${userId.id}"
    }
}

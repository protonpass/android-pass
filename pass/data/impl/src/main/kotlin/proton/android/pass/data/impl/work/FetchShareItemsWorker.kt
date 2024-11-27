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

package proton.android.pass.data.impl.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.impl.R
import proton.android.pass.data.impl.repositories.FetchShareItemsStatus
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger

@HiltWorker
open class FetchShareItemsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val fetchShareItemsStatusRepository: FetchShareItemsStatusRepository,
    private val itemRepository: ItemRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")

        val userId = inputData.getString(ARG_USER_ID)
            ?.let(::UserId)
            ?: return Result.failure()

        val shareId = inputData.getString(ARG_SHARE_ID)
            ?.let(::ShareId)
            ?: return Result.failure()

        fetchShareItemsStatusRepository.emit(shareId, FetchShareItemsStatus.NotStarted)

        return runCatching {
            itemRepository.downloadItemsAndObserveProgress(
                userId = userId,
                shareId = shareId,
                onProgress = {}
            ).let { itemRevisions ->
                itemRepository.setShareItems(
                    userId = userId,
                    items = mapOf(shareId to itemRevisions),
                    onProgress = { progress ->
                        when {
                            progress.total == 0 -> {
                                FetchShareItemsStatus.Done(0)
                            }

                            progress.current == progress.total -> {
                                FetchShareItemsStatus.Done(progress.total)
                            }

                            else -> {
                                FetchShareItemsStatus.Syncing(
                                    current = progress.current,
                                    total = progress.total
                                )
                            }
                        }.also { fetchShareItemsStatus ->
                            fetchShareItemsStatusRepository.emit(shareId, fetchShareItemsStatus)
                            PassLogger.d(TAG, "ShareId $shareId progress: $fetchShareItemsStatus")
                        }
                    }
                )
            }
        }.fold(
            onSuccess = {
                PassLogger.i(TAG, "$TAG finished successfully")
                Result.success()
            },
            onFailure = {
                PassLogger.w(TAG, "$TAG failed")
                Result.retry()
            }
        )
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        SYNC_NOTIFICATION_ID,
        context.fetchItemsWorkNotification()
    )

    private fun Context.fetchItemsWorkNotification(): Notification {
        val channel = NotificationChannel(
            SYNC_NOTIFICATION_CHANNEL_ID,
            getString(R.string.sync_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = getString(R.string.sync_channel_description) }
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(me.proton.core.notification.R.drawable.ic_proton_brand_proton_pass)
            .setContentTitle(getString(R.string.syncing_vaults))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    internal companion object {

        private const val TAG = "FetchShareItemsWorker"

        private const val ARG_SHARE_ID = "share_id"
        private const val ARG_USER_ID = "user_id"

        private const val SYNC_NOTIFICATION_ID = 0
        private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

        internal fun getRequestFor(userId: UserId, shareId: ShareId): WorkRequest {
            val extras = mutableMapOf(
                ARG_SHARE_ID to shareId.id,
                ARG_USER_ID to userId.id
            )

            val data = Data.Builder()
                .putAll(extras.toMap())
                .build()

            return OneTimeWorkRequestBuilder<FetchShareItemsWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(data)
                .build()
        }

    }

}

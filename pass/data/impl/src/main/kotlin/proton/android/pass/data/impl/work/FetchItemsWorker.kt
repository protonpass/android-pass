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
import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.usecases.sync.ForceSyncItems
import proton.android.pass.data.api.usecases.sync.ForceSyncResult
import proton.android.pass.data.impl.R
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import me.proton.core.notification.R as CoreR

@HiltWorker
open class FetchItemsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val forceSyncItems: ForceSyncItems,
    private val accountManager: AccountManager,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")

        val userId = accountManager.getPrimaryUserId().first() ?: return Result.failure()
        val shareIds = inputData.getStringArray(ARG_SHARE_IDS)?.map { ShareId(it) } ?: emptyList()
        val fetchSource = inputData.getString(ARG_FETCH_SOURCE)?.let { FetchSource.valueOf(it) }
        if (fetchSource == null) {
            PassLogger.w(TAG, "Invalid fetch source")
            return Result.failure()
        }

        PassLogger.i(TAG, "Fetching items for ${shareIds.size} shares in ${fetchSource.name}")

        val res = forceSyncItems(userId, shareIds, isBackground = !fetchSource.showDialog)
        return when (res) {
            ForceSyncResult.Error -> {
                PassLogger.i(TAG, "$TAG finished with errors")
                Result.failure()
                Result.retry()
            }
            ForceSyncResult.Success -> {
                PassLogger.i(TAG, "$TAG finished successfully")
                Result.success()
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        SYNC_NOTIFICATION_ID,
        context.syncWorkNotification()
    )

    private fun Context.syncWorkNotification(): Notification {
        val channel = NotificationChannel(
            SYNC_NOTIFICATION_CHANNEL_ID,
            getString(R.string.sync_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = getString(R.string.sync_channel_description) }
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(CoreR.drawable.ic_proton_brand_proton_pass)
            .setContentTitle(getString(R.string.syncing_vaults))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    enum class FetchSource(val showDialog: Boolean) {
        ForceSync(true),
        NewShare(false)
    }

    companion object {
        private const val TAG = "FetchItemsWorker"
        private const val ARG_SHARE_IDS = "share_ids"
        private const val ARG_FETCH_SOURCE = "fetch_source"

        private const val SYNC_NOTIFICATION_ID = 0
        private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

        fun getRequestFor(source: FetchSource, shareIds: List<ShareId>): WorkRequest {
            val shareIdsAsString = shareIds.map { it.id }.toTypedArray()
            val extras = mutableMapOf(
                ARG_SHARE_IDS to shareIdsAsString,
                ARG_FETCH_SOURCE to source.name
            )

            val data = Data.Builder()
                .putAll(extras.toMap())
                .build()

            return OneTimeWorkRequestBuilder<FetchItemsWorker>()
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

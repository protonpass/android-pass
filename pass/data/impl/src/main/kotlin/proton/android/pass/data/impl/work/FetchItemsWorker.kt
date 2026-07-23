/*
 * Copyright (c) 2023-2026 Proton AG
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
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
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
    private val shareRepository: ShareRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")

        val userId = inputData.getString(ARG_USER_ID)?.let(::UserId) ?: return Result.failure()
        val hasInactiveShares = inputData.getBoolean(ARG_INACTIVE_SHARES, false)
        val hasInvalidGroupShares = inputData.getBoolean(ARG_INVALID_GROUP_SHARES, false)
        val origin = inputData.getString(ARG_ORIGIN) ?: UNKNOWN_ORIGIN

        val fetchSource = getFetchSource() ?: return Result.failure()

        val shareIds: Set<ShareId> = when (fetchSource) {
            is FetchSource.ForceSync,
            is FetchSource.FirstSync ->
                shareRepository.observeAllShares(userId, includeHidden = true)
                    .first()
                    .map { it.id }
                    .toSet()
            is FetchSource.NewShare -> fetchSource.shareIds
        }

        PassLogger.i(
            TAG,
            "Fetching items for ${shareIds.size} shares in ${fetchSource::class.simpleName} " +
                "(origin=$origin)"
        )

        val res = forceSyncItems(
            userId = userId,
            shareIds = shareIds,
            hasInactiveShares = hasInactiveShares,
            hasInvalidGroupShares = hasInvalidGroupShares
        )
        return when (res) {
            ForceSyncResult.Error -> {
                PassLogger.i(TAG, "$TAG finished with errors")
                Result.retry()
            }

            ForceSyncResult.PartialSuccess -> {
                PassLogger.w(TAG, "$TAG finished with partial success")
                Result.success()
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

    private fun getFetchSource(): FetchSource? = when (inputData.getString(ARG_FETCH_SOURCE)) {
        SOURCE_FORCE_SYNC -> FetchSource.ForceSync
        SOURCE_FIRST_SYNC -> FetchSource.FirstSync
        SOURCE_NEW_SHARE -> FetchSource.NewShare(
            inputData.getStringArray(ARG_SHARE_IDS)
                ?.map(::ShareId)
                ?.toSet()
                ?: emptySet()
        )

        else -> {
            PassLogger.w(TAG, "Invalid fetch source")
            null
        }
    }

    private fun Context.syncWorkNotification(): Notification {
        val channel = NotificationChannel(
            SYNC_NOTIFICATION_CHANNEL_ID,
            getString(R.string.sync_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = getString(R.string.sync_channel_description) }
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
            ?.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, SYNC_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(CoreR.drawable.ic_proton_brand_proton_pass)
            .setContentTitle(getString(R.string.syncing_vaults))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    sealed class FetchSource {
        data object ForceSync : FetchSource()
        data object FirstSync : FetchSource()
        data class NewShare(val shareIds: Set<ShareId>) : FetchSource()
    }

    data class SyncWarnings(
        val hasInactiveShares: Boolean,
        val hasInvalidGroupShares: Boolean
    )

    companion object {
        private const val TAG = "FetchItemsWorker"
        private const val ARG_USER_ID = "user_id"
        private const val ARG_SHARE_IDS = "share_ids"
        private const val ARG_FETCH_SOURCE = "fetch_source"
        private const val ARG_INACTIVE_SHARES = "inactive_shares"
        private const val ARG_INVALID_GROUP_SHARES = "invalid_group_shares"
        private const val ARG_ORIGIN = "origin"

        private const val SYNC_NOTIFICATION_ID = 0
        private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"

        private const val SOURCE_FORCE_SYNC = "ForceSync"
        private const val SOURCE_FIRST_SYNC = "FirstSync"
        private const val SOURCE_NEW_SHARE = "NewShare"
        private const val UNKNOWN_ORIGIN = "unknown"

        fun getRequestFor(
            source: FetchSource,
            userId: UserId,
            origin: String = UNKNOWN_ORIGIN,
            warnings: SyncWarnings
        ): OneTimeWorkRequest {
            val extras = mutableMapOf<String, Any>(
                ARG_FETCH_SOURCE to source.sourceName(),
                ARG_USER_ID to userId.id,
                ARG_ORIGIN to origin,
                ARG_INACTIVE_SHARES to warnings.hasInactiveShares,
                ARG_INVALID_GROUP_SHARES to warnings.hasInvalidGroupShares
            )
            if (source is FetchSource.NewShare) {
                extras[ARG_SHARE_IDS] = source.shareIds.map { it.id }.toTypedArray()
            }

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

        private fun FetchSource.sourceName(): String = when (this) {
            is FetchSource.ForceSync -> SOURCE_FORCE_SYNC
            is FetchSource.FirstSync -> SOURCE_FIRST_SYNC
            is FetchSource.NewShare -> SOURCE_NEW_SHARE
        }

        fun getOneTimeUniqueWorkName(userId: UserId?) = "${FetchItemsWorker::class.simpleName}-one-time-$userId"
    }
}

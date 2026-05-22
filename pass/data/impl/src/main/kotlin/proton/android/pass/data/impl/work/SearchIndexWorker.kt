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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.log.api.PassLogger

@HiltWorker
class SearchIndexWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val searchIndexRepository: SearchIndexRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting SearchIndexWorker attempt $runAttemptCount")

        val userId = inputData.getString(ARG_USER_ID)?.let(::UserId)

        if (userId == null) {
            PassLogger.w(TAG, "UserId is null, cannot rebuild index")
            return Result.failure()
        }

        return runCatching {
            PassLogger.i(TAG, "Rebuilding search index for user: $userId")
            searchIndexRepository.rebuildIndex(userId)
            PassLogger.i(TAG, "Search index rebuild completed successfully")
            Result.success()
        }.fold(
            onSuccess = { it },
            onFailure = {
                PassLogger.e(TAG, it, "Error rebuilding search index")
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        )
    }

    companion object {
        private const val TAG = "SearchIndexWorker"
        private const val ARG_USER_ID = "user_id"
        private const val MAX_RETRIES = 3

        fun getWorkName(userId: UserId) = "${SearchIndexWorker::class.simpleName}-${userId.id}"

        fun getRequest(userId: UserId): OneTimeWorkRequest {
            val data = Data.Builder()
                .putString(ARG_USER_ID, userId.id)
                .build()

            return OneTimeWorkRequestBuilder<SearchIndexWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setInputData(data)
                .build()
        }

        fun enqueue(context: Context, userId: UserId) {
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniqueWork(
                getWorkName(userId),
                ExistingWorkPolicy.KEEP,
                getRequest(userId)
            )
            PassLogger.i(TAG, "Enqueued search index rebuild for user: $userId")
        }
    }
}

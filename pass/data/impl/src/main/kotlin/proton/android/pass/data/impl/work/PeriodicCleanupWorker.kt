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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import proton.android.pass.files.api.DirectoryCleaner
import proton.android.pass.files.api.DirectoryType
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit

@HiltWorker
class PeriodicCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val directoryCleaner: DirectoryCleaner
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = runCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        coroutineScope {
            listOf(
                async { directoryCleaner.deleteDir(DirectoryType.CameraTemp) },
                async { directoryCleaner.deleteDir(DirectoryType.ShareTemp) },
                async { directoryCleaner.deleteDir(DirectoryType.OrphanedAttachments) }
            ).awaitAll()
        }
    }.onSuccess {
        PassLogger.i(TAG, "Finished $TAG")
    }.onFailure {
        PassLogger.w(TAG, "Failed to clean cache")
        PassLogger.w(TAG, it)
    }.toWorkerResult()

    companion object {
        const val WORKER_UNIQUE_NAME = "periodic_cleanup_worker"
        private const val TAG = "PeriodicCleanupWorker"
        private const val REPEAT_DAYS = 1L

        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicCleanupWorker>(REPEAT_DAYS, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresDeviceIdle(true)
                        .build()
                )
                .build()
    }
}




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
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.impl.local.LocalPopularServicesDataSource
import proton.android.pass.data.impl.remote.popularservices.RemotePopularServicesDataSource
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit

@HiltWorker
class PeriodicPopularServicesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val remotePopularServicesDataSource: RemotePopularServicesDataSource,
    private val localPopularServicesDataSource: LocalPopularServicesDataSource
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = safeRunCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        val services = remotePopularServicesDataSource.fetch()
        localPopularServicesDataSource.store(services).getOrThrow()
    }.onSuccess {
        PassLogger.i(TAG, "Finished $TAG")
    }.onFailure {
        PassLogger.w(TAG, "Failed to refresh popular services")
        PassLogger.w(TAG, it)
    }.toWorkerResult()

    companion object {
        const val WORKER_UNIQUE_NAME = "periodic_popular_services_worker"
        private const val TAG = "PeriodicPopularServicesWorker"
        private const val REPEAT_DAYS = 7L

        // No initial delay: the first run is scheduled as soon as the network constraint is met
        // (≈ first app start), then it repeats weekly.
        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicPopularServicesWorker>(REPEAT_DAYS, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
    }
}

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

package proton.android.pass.data.impl.usecases

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.data.api.usecases.UserPlanWorkerLauncher
import proton.android.pass.data.impl.work.UserAccessWorker
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserPlanWorkerLauncherImpl @Inject constructor(
    private val workManager: WorkManager,
    private val eventWorkerManager: EventWorkerManager,
) : UserPlanWorkerLauncher {

    override fun start() {
        val backoffDelaySeconds = eventWorkerManager.getBackoffDelay().inWholeSeconds
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest: PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<UserAccessWorker>(1, TimeUnit.DAYS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    backoffDelaySeconds,
                    TimeUnit.SECONDS
                )
                .setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork(
            UserAccessWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun cancel() {
        PassLogger.i(TAG, "Cancelling UserAccessWorker")
        workManager.cancelUniqueWork(UserAccessWorker.WORKER_UNIQUE_NAME)
    }

    companion object {
        private const val TAG = "UserPlanWorkerLauncherImpl"
    }
}

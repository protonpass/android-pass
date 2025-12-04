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

package proton.android.pass.data.impl.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@HiltWorker
open class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val performSync: PerformSync,
    private val accountManager: AccountManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")

        val result = accountManager.getAccounts(AccountState.Ready)
            .mapLatest { accounts ->
                accounts.map { account ->
                    safeRunCatching { performSync(account.userId) }
                        .onSuccess {
                            PassLogger.i(TAG, "Sync for ${account.userId} finished successfully")
                        }
                        .onFailure {
                            PassLogger.w(TAG, "Sync for ${account.userId} finished with error")
                            PassLogger.w(TAG, it)
                        }
                }
            }
            .first()
            .transpose()

        return if (result.isSuccess) {
            PassLogger.i(TAG, "SyncWorker finished successfully")
            Result.success()
        } else {
            PassLogger.w(TAG, "SyncWorker finished with error")
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"

        const val WORKER_UNIQUE_NAME = "sync_worker"

        fun getRequestFor(manager: EventWorkerManager, initialDelay: Duration): PeriodicWorkRequest {
            val initialDelaySeconds = initialDelay.inWholeSeconds
            val backoffDelaySeconds = manager.getBackoffDelay().inWholeSeconds
            val repeatIntervalSeconds = manager.getRepeatIntervalBackground().inWholeSeconds
            return PeriodicWorkRequestBuilder<SyncWorker>(repeatIntervalSeconds, TimeUnit.SECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    backoffDelaySeconds,
                    TimeUnit.SECONDS
                )
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .build()
        }
    }
}

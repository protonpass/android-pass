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
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.compromisedpassword.RefreshCompromisedPasswords
import proton.android.pass.data.api.usecases.items.ObserveMonitoredItems
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit

@HiltWorker
class PeriodicCompromisedPasswordWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val accountManager: AccountManager,
    private val observeMonitoredItems: ObserveMonitoredItems,
    private val refreshCompromisedPasswords: RefreshCompromisedPasswords
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = safeRunCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        accountManager.getAccounts(AccountState.Ready).firstOrNull().orEmpty()
            .forEach { account ->
                val items = observeMonitoredItems(account.userId, includeHiddenVaults = false)
                    .firstOrNull()
                    .orEmpty()
                refreshCompromisedPasswords(account.userId, items)
            }
    }.onSuccess {
        PassLogger.i(TAG, "Finished $TAG")
    }.onFailure {
        PassLogger.w(TAG, "Failed to run $TAG")
        PassLogger.w(TAG, it)
    }.fold(
        onSuccess = { Result.success() },
        onFailure = { Result.retry() }
    )

    companion object {
        const val WORKER_UNIQUE_NAME = "periodic_compromised_password_worker"
        private const val TAG = "PeriodicCompromisedPasswordWorker"
        private const val REPEAT_DAYS = 7L
        private const val INITIAL_DELAY_MINUTES = 5L

        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<PeriodicCompromisedPasswordWorker>(REPEAT_DAYS, TimeUnit.DAYS)
                .setInitialDelay(INITIAL_DELAY_MINUTES, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}

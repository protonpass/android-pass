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

import androidx.lifecycle.coroutineScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.impl.sync.SyncWorker.Companion.WORKER_UNIQUE_NAME
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class SyncManagerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val eventWorkerManager: EventWorkerManager,
    private val performSync: PerformSync,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val accountManager: AccountManager
) : SyncManager {

    data class SyncState(
        val userId: UserId?,
        val appLifecycle: AppLifecycleProvider.State
    )

    override fun start() {
        PassLogger.i(TAG, "SyncManager start")
        appLifecycleProvider.lifecycle.coroutineScope.launch {
            combine(
                appLifecycleProvider.state,
                accountManager.getPrimaryUserId().flowOn(Dispatchers.IO)
            ) { appLifecycle, userId ->
                SyncState(userId, appLifecycle)
            }
                .catch { PassLogger.w(TAG, it) }
                .collectLatest {
                    if (it.userId != null) {
                        onUserLoggedInPerformSync(it.userId, it.appLifecycle)
                    } else {
                        cancelWorker()
                    }
                }
        }
    }

    private suspend fun onUserLoggedInPerformSync(
        userId: UserId?,
        state: AppLifecycleProvider.State
    ) {
        val initialDelay = when (state) {
            AppLifecycleProvider.State.Background -> eventWorkerManager.getRepeatIntervalBackground()
            AppLifecycleProvider.State.Foreground -> eventWorkerManager.getRepeatIntervalForeground()
        }
        when (state) {
            AppLifecycleProvider.State.Foreground -> {
                cancelWorker()
                while (currentCoroutineContext().isActive) {
                    runCatching { performSync(userId) }
                        .onSuccess { PassLogger.i(TAG, "Sync finished") }
                        .onFailure { error ->
                            PassLogger.w(TAG, "Error in performSync")
                            PassLogger.w(TAG, error)
                        }

                    delay(initialDelay)
                }
            }

            AppLifecycleProvider.State.Background -> {
                enqueueWorker(initialDelay)
            }
        }
    }

    private fun enqueueWorker(initialDelay: Duration) {
        val request = SyncWorker.getRequestFor(eventWorkerManager, initialDelay)
        workManager.enqueueUniquePeriodicWork(
            WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        PassLogger.i(TAG, "$WORKER_UNIQUE_NAME enqueued")
    }

    private fun cancelWorker() {
        workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
        PassLogger.i(TAG, "$WORKER_UNIQUE_NAME cancelled")
    }

    companion object {
        private const val TAG = "SyncManagerImpl"
    }
}

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

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.impl.sync.SyncWorker.Companion.WORKER_UNIQUE_NAME
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class SyncManagerImpl @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val workManager: WorkManager,
    private val eventWorkerManager: EventWorkerManager,
    private val performSync: PerformSync,
    appLifecycleProvider: AppLifecycleProvider,
    accountManager: AccountManager
) : SyncManager {

    private val state: Flow<SyncState> = combine(
        accountManager.getPrimaryUserId(),
        appLifecycleProvider.state
    ) { userId, appLifecycle ->
        SyncState(userId != null, appLifecycle)
    }

    data class SyncState(
        val isLoggedIn: Boolean,
        val appLifecycle: AppLifecycleProvider.State
    )

    override fun start() {
        PassLogger.i(TAG, "SyncManager start")
        scopeProvider.GlobalIOSupervisedScope.launch {
            state.collectLatest {
                if (it.isLoggedIn) {
                    val initialDelay = when (it.appLifecycle) {
                        AppLifecycleProvider.State.Background -> eventWorkerManager.getRepeatIntervalBackground()
                        AppLifecycleProvider.State.Foreground -> eventWorkerManager.getRepeatIntervalForeground()
                    }
                    when (it.appLifecycle) {
                        AppLifecycleProvider.State.Foreground -> {
                            workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
                            while (currentCoroutineContext().isActive) {
                                performSync()
                                    .onFailure { error ->
                                        PassLogger.w(TAG, "Error in performSync")
                                        PassLogger.w(TAG, error)
                                    }
                                delay(initialDelay)
                            }
                        }

                        AppLifecycleProvider.State.Background -> enqueueWorker(initialDelay)
                    }
                } else {
                    workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
                    PassLogger.i(TAG, "$WORKER_UNIQUE_NAME cancelled")
                }
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

    companion object {
        private const val TAG = "SyncManagerImpl"
    }
}

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

package proton.android.pass.data.impl.sync

import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.presentation.app.AppLifecycleProvider
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.SearchIndexRepository
import proton.android.pass.data.api.usecases.ObserveReadyAccounts
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.impl.sync.SyncWorker.Companion.WORKER_UNIQUE_NAME
import proton.android.pass.domain.account.Account
import proton.android.pass.log.api.LogAccountContext
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
    private val observeReadyAccounts: ObserveReadyAccounts,
    private val searchIndexRepository: SearchIndexRepository
) : SyncManager {

    private var rebuildJob: Job? = null

    override fun start() {
        PassLogger.i(TAG, "SyncManager start")

        observeReadyAccounts()
            .combine(appLifecycleProvider.state, ::Pair)
            .mapLatest { (accounts, state) -> onAccountsIsReady(accounts, state) }
            .flowWithLifecycle(appLifecycleProvider.lifecycle)
            .launchIn(appLifecycleProvider.lifecycle.coroutineScope)
    }

    private suspend fun onAccountsIsReady(accounts: List<Account>, state: AppLifecycleProvider.State) {
        when (state) {
            AppLifecycleProvider.State.Background -> {
                enqueueWorker(eventWorkerManager.getRepeatIntervalBackground())
            }

            AppLifecycleProvider.State.Foreground -> {
                cancelWorker()

                // Check and rebuild search index in a separate coroutine
                // Cancel any previous rebuild to avoid concurrent jobs piling up
                rebuildJob?.cancel()
                rebuildJob = appLifecycleProvider.lifecycle.coroutineScope.launch {
                    accounts.forEach { account ->
                        withContext(LogAccountContext(account.userId)) {
                            safeRunCatching { searchIndexRepository.checkAndRebuildIfNeeded(account.userId) }
                                .onFailure { error ->
                                    PassLogger.w(TAG, "Error checking search index rebuild")
                                    PassLogger.w(TAG, error)
                                }
                        }
                    }
                }

                val backoff = SyncBackoff()
                while (currentCoroutineContext().isActive) {
                    var anyFailed = false
                    accounts.forEach { account ->
                        withContext(LogAccountContext(account.userId)) {
                            safeRunCatching {
                                performSync(userId = account.userId, trigger = "foreground_manager")
                                    .getOrThrow()
                            }
                                .onSuccess { PassLogger.i(TAG, "Sync finished") }
                                .onFailure { error ->
                                    anyFailed = true
                                    PassLogger.w(TAG, "Error in performSync")
                                    PassLogger.w(TAG, error)
                                }
                        }
                    }

                    if (anyFailed) backoff.onFailure() else backoff.onSuccess()

                    delay(backoff.nextDelay(eventWorkerManager.getRepeatIntervalForeground()))
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

    private fun cancelWorker() {
        workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
        PassLogger.i(TAG, "$WORKER_UNIQUE_NAME cancelled")
    }

    private companion object {

        private const val TAG = "SyncManagerImpl"

    }

}

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

package proton.android.pass.telemetry.impl.startup

import androidx.lifecycle.coroutineScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.presentation.app.AppLifecycleProvider
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.telemetry.impl.work.TelemetrySenderWorker
import proton.android.pass.telemetry.impl.work.TelemetrySenderWorker.Companion.WORKER_UNIQUE_NAME
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

interface TelemetryStartupManager {
    fun start()
}

class TelemetryStartupManagerImpl @Inject constructor(
    private val appLifecycleProvider: AppLifecycleProvider,
    private val workManager: WorkManager,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager
) : TelemetryStartupManager {

    override fun start() {
        PassLogger.i(TAG, "TelemetryStartupManager start")
        appLifecycleProvider.lifecycle.coroutineScope.launch {
            launch { startListener() }
            startWorker()
        }
    }

    private suspend fun startWorker() {
        accountManager.getPrimaryUserId()
            .flowOn(Dispatchers.IO)
            .collectLatest {
                if (it == null) {
                    cancelWorker()
                } else {
                    enqueueWorker()
                }
            }
    }

    private fun enqueueWorker() {
        val initialDelay = Random.nextInt(1, 3)
        val request = TelemetrySenderWorker.getRequestFor(
            repeatInterval = SEND_TELEMETRY_INTERVAL,
            initialDelay = initialDelay.hours
        )
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

    private suspend fun startListener() {
        telemetryManager.startListening(
            onSubscribed = {
                PassLogger.i(TAG, "TelemetryManager ready for receiving events")
            },
            onPerformed = {}
        )
    }

    companion object {
        private const val TAG = "TelemetryStartupManagerImpl"

        private val SEND_TELEMETRY_INTERVAL = 6.hours
    }
}

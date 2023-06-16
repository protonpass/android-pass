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

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.util.kotlin.CoroutineScopeProvider
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
    private val scopeProvider: CoroutineScopeProvider,
    private val workManager: WorkManager,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager
) : TelemetryStartupManager {

    override fun start() {
        PassLogger.i(TAG, "TelemetryStartupManager start")
        scopeProvider.GlobalIOSupervisedScope.launch {
            launch { startListener() }
            startWorker()
        }
    }

    private suspend fun startWorker() {
        accountManager.getPrimaryUserId().collectLatest {
            if (it == null) {
                // User not logged in
                workManager.cancelUniqueWork(WORKER_UNIQUE_NAME)
                PassLogger.i(TAG, "$WORKER_UNIQUE_NAME cancelled")
            } else {
                enqueueWorker()
                PassLogger.i(TAG, "$WORKER_UNIQUE_NAME enqueued")
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

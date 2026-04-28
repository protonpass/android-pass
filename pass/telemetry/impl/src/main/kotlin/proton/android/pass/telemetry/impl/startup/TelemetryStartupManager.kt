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

package proton.android.pass.telemetry.impl.startup

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.presentation.app.AppLifecycleProvider
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.telemetry.api.InstallReferrerProvider
import proton.android.pass.telemetry.api.TelemetryGrowthInstallEvent
import proton.android.pass.telemetry.api.TelemetryGrowthOpenEvent
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.telemetry.impl.work.LiveTelemetrySenderWorker
import proton.android.pass.telemetry.impl.work.TelemetrySenderWorker
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

interface TelemetryStartupManager {
    fun start()
}

class TelemetryStartupManagerImpl @Inject constructor(
    private val appLifecycleProvider: AppLifecycleProvider,
    private val workManager: WorkManager,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val installReferrerProvider: InstallReferrerProvider
) : TelemetryStartupManager {

    private var isFirstForeground = true

    override fun start() {
        PassLogger.i(TAG, "TelemetryStartupManager start")
        appLifecycleProvider.lifecycle.coroutineScope.launch {
            launch { startListener() }
            launch { observeAppLifecycle() }
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
        enqueueDeferredTelemetryWorker()
        enqueueLiveTelemetryWorker()
    }

    private fun enqueueDeferredTelemetryWorker() {
        val initialDelay = Random.nextInt(1, 3)
        val request = TelemetrySenderWorker.getRequestFor(
            repeatInterval = DEFERRED_TELEMETRY_INTERVAL,
            initialDelay = initialDelay.hours
        )
        workManager.enqueueUniquePeriodicWork(
            TelemetrySenderWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        PassLogger.i(TAG, "${TelemetrySenderWorker.WORKER_UNIQUE_NAME} enqueued")
    }

    private fun enqueueLiveTelemetryWorker() {
        val request = LiveTelemetrySenderWorker.getRequestFor(LIVE_TELEMETRY_INTERVAL)
        workManager.enqueueUniquePeriodicWork(
            LiveTelemetrySenderWorker.WORKER_UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        PassLogger.i(TAG, "${LiveTelemetrySenderWorker.WORKER_UNIQUE_NAME} enqueued")
    }

    private fun cancelWorker() {
        workManager.cancelUniqueWork(TelemetrySenderWorker.WORKER_UNIQUE_NAME)
        PassLogger.i(TAG, "${TelemetrySenderWorker.WORKER_UNIQUE_NAME} cancelled")

        workManager.cancelUniqueWork(LiveTelemetrySenderWorker.WORKER_UNIQUE_NAME)
        PassLogger.i(TAG, "${LiveTelemetrySenderWorker.WORKER_UNIQUE_NAME} cancelled")
    }

    private suspend fun startListener() {
        telemetryManager.startListening(
            onSubscribed = {
                PassLogger.i(TAG, "TelemetryManager ready for receiving events")
            },
            onPerformed = {}
        )
    }

    private fun observeAppLifecycle() {
        appLifecycleProvider.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                appLifecycleProvider.lifecycle.coroutineScope.launch {
                    handleForegroundTransition()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                internalSettingsRepository.setLastBackgroundTimestamp(System.currentTimeMillis())
            }
        })
    }

    private suspend fun handleForegroundTransition() {
        if (isFirstForeground) {
            isFirstForeground = false
            sendInstallEventIfNeeded()
            sendOpenEvent()
        } else {
            val lastBackground = internalSettingsRepository.getLastBackgroundTimestamp().first()
            if (lastBackground > 0) {
                val inactivityMs = System.currentTimeMillis() - lastBackground
                if (inactivityMs >= SESSION_INACTIVITY_THRESHOLD.inWholeMilliseconds) {
                    sendOpenEvent()
                }
            }
        }
    }

    private fun sendOpenEvent() {
        telemetryManager.sendEvent(TelemetryGrowthOpenEvent)
    }

    private suspend fun sendInstallEventIfNeeded() {
        val alreadySent = internalSettingsRepository.hasTelemetryGrowthInstallEventBeenSent().first()
        if (!alreadySent) {
            val installRef = installReferrerProvider.getInstallReferrer()
            PassLogger.i(TAG, "Sending TelemetryGrowth install event (installRef=${installRef != null})")
            telemetryManager.sendEvent(TelemetryGrowthInstallEvent(installRef = installRef))
            internalSettingsRepository.setTelemetryGrowthInstallEventSent(true)
        }
    }

    companion object {
        private const val TAG = "TelemetryStartupManagerImpl"

        private val DEFERRED_TELEMETRY_INTERVAL = 6.hours
        private val LIVE_TELEMETRY_INTERVAL = 15.minutes
        private val SESSION_INACTIVITY_THRESHOLD = 30.minutes
    }
}

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

package proton.android.pass.telemetry.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryManagerImpl @Inject constructor(
    private val deferredTelemetryManager: DeferredTelemetryManager,
    private val liveTelemetryManager: LiveTelemetryManager
) : TelemetryManager {

    override fun sendEvent(event: TelemetryEvent) {
        when (event) {
            is TelemetryEvent.DeferredTelemetryEvent -> {
                deferredTelemetryManager.sendEvent(event)
            }

            is TelemetryEvent.LiveTelemetryEvent -> {
                liveTelemetryManager.sendEvent(event)
            }
        }
    }

    override suspend fun startListening(onSubscribed: () -> Unit, onPerformed: () -> Unit) {
        coroutineScope {
            launch {
                deferredTelemetryManager.startListening(
                    onSubscribed = {
                        PassLogger.i(TAG, "Subscribed to deferred telemetry")
                        onSubscribed()
                    },
                    onPerformed = onPerformed
                )
            }
            launch {
                liveTelemetryManager.startListening(
                    onSubscribed = {
                        PassLogger.i(TAG, "Subscribed to live telemetry")
                        onSubscribed()
                    },
                    onPerformed = onPerformed
                )
            }
        }
    }

    companion object {
        private const val TAG = "TelemetryManagerImpl"
    }
}

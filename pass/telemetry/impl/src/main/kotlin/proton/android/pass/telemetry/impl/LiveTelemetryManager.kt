/*
 * Copyright (c) 2024 Proton AG
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

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onSubscription
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.LiveTelemetryRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryEvent
import javax.inject.Inject
import javax.inject.Singleton

interface LiveTelemetryManager {
    fun sendEvent(event: TelemetryEvent.LiveTelemetryEvent)
    suspend fun startListening(onSubscribed: () -> Unit = {}, onPerformed: () -> Unit = {})
}

@Singleton
class LiveTelemetryManagerImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val repository: LiveTelemetryRepository
) : LiveTelemetryManager {

    private val mutableEventFlow: MutableSharedFlow<TelemetryEvent.LiveTelemetryEvent> =
        MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = BUFFER_CAPACITY,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    override fun sendEvent(event: TelemetryEvent.LiveTelemetryEvent) {
        mutableEventFlow.tryEmit(event)
    }

    override suspend fun startListening(onSubscribed: () -> Unit, onPerformed: () -> Unit) {
        mutableEventFlow
            .onSubscription { onSubscribed() }
            .collect { event ->
                safeRunCatching {
                    performSendEvent(event)
                    onPerformed()
                }.onSuccess {
                    PassLogger.v(TAG, "Event sent successfully")
                }.onFailure {
                    PassLogger.w(TAG, "Error sending event")
                    PassLogger.w(TAG, it)
                }
            }
    }

    private suspend fun performSendEvent(event: TelemetryEvent.LiveTelemetryEvent) {
        val userId = observeCurrentUser().firstOrNull()?.userId ?: return
        repository.sendEvent(userId, event)
    }

    companion object {
        private const val TAG = "LiveTelemetryManagerImpl"
        private const val BUFFER_CAPACITY = 10
    }
}

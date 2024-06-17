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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.telemetry.api.TelemetryEvent
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent
import proton.android.pass.telemetry.api.TelemetryEvent.LiveTelemetryEvent
import proton.android.pass.test.MainDispatcherRule

data object TestEvent : DeferredTelemetryEvent("test")

class TelemetryManagerImplTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: TelemetryManagerImpl
    private lateinit var deferred: FakeDeferredTelemetryManager
    private lateinit var live: FakeLiveTelemetryManager

    @Before
    fun setup() {
        deferred = FakeDeferredTelemetryManager()
        live = FakeLiveTelemetryManager()
        instance = TelemetryManagerImpl(
            deferredTelemetryManager = deferred,
            liveTelemetryManager = live
        )
    }

    @Test
    fun `sent event is stored`() = runTest {
        val startMutex = Mutex()
        startMutex.lock()

        val eventMutex = Mutex()
        eventMutex.lock()

        val job = launch {
            instance.startListening(
                onSubscribed = { if (startMutex.isLocked) startMutex.unlock() },
                onPerformed = { eventMutex.unlock() }
            )
        }

        // Make sure it is subscribed
        startMutex.lock()

        // Send the event
        instance.sendEvent(TestEvent)

        // Make sure the process has been called
        eventMutex.lock()

        val memory = deferred.getMemory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0].eventName).isEqualTo(TestEvent.eventName)

        job.cancel()
    }

    open class FakeTelemetryManager<T : TelemetryEvent> {
        private var onPerformedCallback: (() -> Unit)? = null

        private var memory: MutableList<T> = mutableListOf()
        fun getMemory(): List<T> = memory

        open fun sendEvent(event: T) {
            memory.add(event)
            onPerformedCallback?.invoke()
        }

        open suspend fun startListening(onSubscribed: () -> Unit, onPerformed: () -> Unit) {
            onPerformedCallback = onPerformed
            onSubscribed()
        }
    }

    private class FakeDeferredTelemetryManager :
        FakeTelemetryManager<DeferredTelemetryEvent>(), DeferredTelemetryManager

    private class FakeLiveTelemetryManager :
        FakeTelemetryManager<LiveTelemetryEvent>(), LiveTelemetryManager

}

package proton.android.pass.telemetry.fakes

import proton.android.pass.telemetry.api.TelemetryEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

class TestTelemetryManager @Inject constructor() : TelemetryManager {

    private val memory: MutableList<TelemetryEvent> = mutableListOf()

    fun getMemory(): List<TelemetryEvent> = memory

    override fun sendEvent(event: TelemetryEvent) {
        memory.add(event)
    }

    override suspend fun startListening(onSubscribed: () -> Unit, onPerformed: () -> Unit) {
        throw IllegalStateException("This method should not be invoked")
    }
}

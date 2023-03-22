package proton.android.pass.telemetry.api

interface TelemetryManager {
    fun sendEvent(event: TelemetryEvent)
    suspend fun startListening(onSubscribed: () -> Unit = {}, onPerformed: () -> Unit = {})
}

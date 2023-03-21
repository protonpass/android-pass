package proton.android.pass.telemetry.api

interface TelemetryManager {
    fun sendEvent(event: TelemetryEvent)
}

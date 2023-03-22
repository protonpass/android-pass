package proton.android.pass.autofill

import proton.android.pass.telemetry.api.TelemetryEvent

enum class AutofillTriggerSource(val source: String) {
    Source("source"),
    App("app")
}

object AutosaveDone : TelemetryEvent("autosave.done")
object AutosaveDisplay : TelemetryEvent("autosave.display")

data class AutofillDisplayed(val source: AutofillTriggerSource) : TelemetryEvent("autofill.display") {
    override fun dimensions(): Map<String, String> = mapOf("location" to source.source)
}
data class AutofillDone(val source: AutofillTriggerSource) : TelemetryEvent("autofill.triggered") {
    override fun dimensions(): Map<String, String> = mapOf("location" to source.source)
}

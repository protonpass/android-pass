package proton.android.pass.featurehome.impl

import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent

data class ItemDelete(val itemType: EventItemType) : TelemetryEvent("item.deletion") {
    override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
}
object SearchTriggered : TelemetryEvent("search.triggered")
object SearchItemClicked : TelemetryEvent("search.click")

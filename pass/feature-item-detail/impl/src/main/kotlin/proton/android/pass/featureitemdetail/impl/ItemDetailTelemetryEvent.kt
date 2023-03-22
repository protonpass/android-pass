package proton.android.pass.featureitemdetail.impl

import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent

data class ItemRead(val itemType: EventItemType) : TelemetryEvent("item.read") {
    override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
}

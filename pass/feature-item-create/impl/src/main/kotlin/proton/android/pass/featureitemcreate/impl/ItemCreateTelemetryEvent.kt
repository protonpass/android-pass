package proton.android.pass.featureitemcreate.impl

import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent

data class ItemCreate(val itemType: EventItemType) : TelemetryEvent("item.creation") {
    override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
}

data class ItemUpdate(val itemType: EventItemType) : TelemetryEvent("item.update") {
    override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
}

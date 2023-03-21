package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class TelemetryRequest(
    @SerialName("EventInfo")
    val eventInfo: List<EventInfo>
)

@Serializable
data class EventInfo(
    @SerialName("MeasurementGroup")
    val measurementGroup: String,
    @SerialName("Event")
    val event: String,
    @SerialName("Values")
    val values: Map<String, JsonPrimitive>,
    @SerialName("Dimensions")
    val dimensions: Map<String, JsonPrimitive>
)

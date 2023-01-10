package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastEventIdResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("EventID")
    val eventId: String
)

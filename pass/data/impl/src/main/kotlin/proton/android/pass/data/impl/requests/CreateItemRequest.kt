package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String,
    @SerialName("ItemKey")
    val itemKey: String
)

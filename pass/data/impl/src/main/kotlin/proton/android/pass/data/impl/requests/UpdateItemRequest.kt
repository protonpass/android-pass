package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemRequest(
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("LastRevision")
    val lastRevision: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String,
)

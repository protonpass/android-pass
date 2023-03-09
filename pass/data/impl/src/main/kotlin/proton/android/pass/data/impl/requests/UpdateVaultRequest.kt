package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateVaultRequest(
    @SerialName("Content")
    val content: String,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("KeyRotation")
    val keyRotation: Long

)

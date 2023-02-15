package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetItemLatestKeyResponse(
    @SerialName("Key")
    val key: ItemLatestKeyResponse
)

@Serializable
data class ItemLatestKeyResponse(
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("Key")
    val key: String
)

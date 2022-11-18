package me.proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetKeyPacketResponse(
    @SerialName("KeyPacketInfo")
    val keyPacketInfo: KeyPacketInfo
)

@Serializable
data class KeyPacketInfo(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("KeyPacket")
    val keyPacket: String,
    @SerialName("KeyPacketSignature")
    val keyPacketSignature: String
)

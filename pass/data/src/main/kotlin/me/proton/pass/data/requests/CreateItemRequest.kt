package me.proton.pass.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("Labels")
    val labels: List<String>,
    @SerialName("VaultKeyPacket")
    val vaultKeyPacket: String,
    @SerialName("VaultKeyPacketSignature")
    val vaultKeyPacketSignature: String,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String,
    @SerialName("UserSignature")
    val userSignature: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String
)

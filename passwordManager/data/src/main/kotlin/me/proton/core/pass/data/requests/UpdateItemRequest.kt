package me.proton.core.pass.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemRequest(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("LastRevision")
    val lastRevision: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String,
    @SerialName("UserSignature")
    val userSignature: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String,
)

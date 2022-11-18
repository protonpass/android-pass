package me.proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetSharesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Shares")
    val shares: List<PartialShareResponse>
)

@Serializable
data class PartialShareResponse(
    @SerialName("ShareID")
    val shareId: String,
    @SerialName("VaultID")
    val vaultId: String,
    @SerialName("TargetType")
    val shareType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("Permission")
    val permission: Int,
    @SerialName("AcceptanceSignature")
    val acceptanceSignature: String?
)

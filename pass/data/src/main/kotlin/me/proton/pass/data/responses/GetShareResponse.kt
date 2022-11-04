package me.proton.pass.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetShareResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Share")
    val share: ShareResponse
)

@Serializable
data class ShareResponse(
    @SerialName("ShareID")
    val shareId: String,
    @SerialName("VaultID")
    val vaultId: String,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("Permission")
    val permission: Int,
    @SerialName("AcceptanceSignature")
    val acceptanceSignature: String,
    @SerialName("InviterEmail")
    val inviterEmail: String,
    @SerialName("InviterAcceptanceSignature")
    val inviterAcceptanceSignature: String,
    @SerialName("SigningKey")
    val signingKey: String,
    @SerialName("SigningKeyPassphrase")
    val signingKeyPassphrase: String?,
    @SerialName("Content")
    val content: String?,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int?,
    @SerialName("ContentRotationID")
    val contentRotationId: String?,
    @SerialName("ContentEncryptedAddressSignature")
    val contentEncryptedAddressSignature: String?,
    @SerialName("ContentEncryptedVaultSignature")
    val contentEncryptedVaultSignature: String?,
    @SerialName("ContentSignatureEmail")
    val contentSignatureEmail: String?,
    @SerialName("ExpireTime")
    val expirationTime: Long?,
    @SerialName("CreateTime")
    val createTime: Long
)

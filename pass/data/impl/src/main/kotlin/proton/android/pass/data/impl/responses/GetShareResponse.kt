package proton.android.pass.data.impl.responses

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
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("TargetType")
    val targetType: Int,
    @SerialName("TargetID")
    val targetId: String,
    @SerialName("Permission")
    val permission: Int,
    @SerialName("Content")
    val content: String?,
    @SerialName("ContentKeyRotation")
    val contentKeyRotation: Long?,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int?,
    @SerialName("ExpireTime")
    val expirationTime: Long?,
    @SerialName("CreateTime")
    val createTime: Long
)

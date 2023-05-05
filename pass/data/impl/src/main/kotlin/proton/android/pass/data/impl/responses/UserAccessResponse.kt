package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccessResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Access")
    val accessResponse: AccessResponse
)

@Serializable
data class AccessResponse(
    @SerialName("Plan")
    val planResponse: PlanResponse
)

@Serializable
data class PlanResponse(
    @SerialName("Type")
    val type: String,
    @SerialName("InternalName")
    val internalName: String,
    @SerialName("DisplayName")
    val displayName: String,
    @SerialName("VaultLimit")
    val vaultLimit: Int?,
    @SerialName("AliasLimit")
    val aliasLimit: Int?,
    @SerialName("TotpLimit")
    val totpLimit: Int?
)

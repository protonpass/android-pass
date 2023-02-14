package proton.android.pass.data.impl.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVaultRequest(
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("Content")
    val content: String,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("EncryptedVaultKey")
    val encryptedVaultKey: String
)

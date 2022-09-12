package me.proton.core.pass.data.requests

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
    @SerialName("ContentEncryptedAddressSignature")
    val contentEncryptedAddressSignature: String,
    @SerialName("ContentEncryptedVaultSignature")
    val contentEncryptedVaultSignature: String,
    @SerialName("VaultKey")
    val vaultKey: String,
    @SerialName("VaultKeyPassphrase")
    val vaultKeyPassphrase: String,
    @SerialName("VaultKeySignature")
    val vaultKeySignature: String,
    @SerialName("KeyPacket")
    val keyPacket: String,
    @SerialName("KeyPacketSignature")
    val keyPacketSignature: String,
    @SerialName("SigningKey")
    val signingKey: String,
    @SerialName("SigningKeyPassphrase")
    val signingKeyPassphrase: String,
    @SerialName("SigningKeyPassphraseKeyPacket")
    val signingKeyPassphraseKeyPacket: String,
    @SerialName("AcceptanceSignature")
    val acceptanceSignature: String,
    @SerialName("ItemKey")
    val itemKey: String,
    @SerialName("ItemKeyPassphrase")
    val itemKeyPassphrase: String,
    @SerialName("ItemKeyPassphraseKeyPacket")
    val itemKeyPassphraseKeyPacket: String,
    @SerialName("ItemKeySignature")
    val itemKeySignature: String
)

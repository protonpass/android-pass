package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetVaultKeysResponse(
    @SerialName("Keys")
    val keys: VaultKeyList
)

@Serializable
data class VaultKeyList(
    @SerialName("Total")
    val total: Long,
    @SerialName("VaultKeys")
    val vaultKeys: List<VaultKeyData>,
    @SerialName("ItemKeys")
    val itemKeys: List<ItemKeyData>
)

@Serializable
data class VaultKeyData(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("Rotation")
    val rotation: Long,
    @SerialName("Key")
    val key: String,
    @SerialName("KeyPassphrase")
    val keyPassphrase: String?,
    @SerialName("KeySignature")
    val keySignature: String,
    @SerialName("CreateTime")
    val createTime: Long
)

@Serializable
data class ItemKeyData(
    @SerialName("RotationID")
    val rotationId: String,
    @SerialName("Key")
    val key: String,
    @SerialName("KeyPassphrase")
    val keyPassphrase: String?,
    @SerialName("KeySignature")
    val keySignature: String,
    @SerialName("CreateTime")
    val createTime: Long
)

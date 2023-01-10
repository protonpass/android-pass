package proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.UserAddress
import proton.pass.domain.key.SigningKey

data class EncryptedVaultItemKeyResponse(
    val vaultKeys: List<EncryptedVaultKeyData>,
    val itemKeys: List<EncryptedItemKeyData>
)

data class EncryptedVaultKeyData(
    val rotationId: String,
    val rotation: Long,
    val key: String,
    val keyPassphrase: String?,
    val keySignature: String,
    val createTime: Long
)

data class EncryptedItemKeyData(
    val rotationId: String,
    val key: String,
    val keyPassphrase: String?,
    val keySignature: String,
    val createTime: Long
)

interface OpenKeys {
    fun open(
        keys: EncryptedVaultItemKeyResponse,
        signingKey: SigningKey,
        userAddress: UserAddress
    ): VaultKeyList
}

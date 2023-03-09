package proton.android.pass.crypto.api.usecases

import proton.pass.domain.key.ShareKey
import proton_pass_vault_v1.VaultV1

data class EncryptedUpdateVaultRequest(
    val content: String,
    val contentFormatVersion: Int,
    val keyRotation: Long
)

interface UpdateVault {
    fun createUpdateVaultRequest(
        shareKey: ShareKey,
        body: VaultV1.Vault
    ): EncryptedUpdateVaultRequest
}

package proton.android.pass.crypto.impl.usecases

import org.apache.commons.codec.binary.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedUpdateVaultRequest
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.pass.domain.key.ShareKey
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class UpdateVaultImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : UpdateVault {

    override fun createUpdateVaultRequest(
        shareKey: ShareKey,
        body: VaultV1.Vault
    ): EncryptedUpdateVaultRequest {

        val decryptedKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val encryptedContent = encryptionContextProvider.withEncryptionContext(decryptedKey) {
            encrypt(body.toByteArray(), EncryptionTag.VaultContent)
        }

        return EncryptedUpdateVaultRequest(
            content = Base64.encodeBase64String(encryptedContent.array),
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            keyRotation = shareKey.rotation
        )
    }

    companion object {
        private const val CONTENT_FORMAT_VERSION = 1
    }
}

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.CreateVaultOutput
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class CreateVaultImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider
) : CreateVault, BaseCryptoOperation(cryptoContext) {

    override fun createVaultRequest(
        user: User,
        userAddress: UserAddress,
        vaultMetadata: VaultV1.Vault
    ): CreateVaultOutput {
        val vaultKey = EncryptionKey.generate()
        val vaultContents = vaultMetadata.toByteArray()

        val encryptedVaultContents = encryptionContextProvider.withEncryptionContext(vaultKey) {
            encrypt(vaultContents, EncryptionTag.VaultContent)
        }
        val encryptedVaultKey = user.useKeys(cryptoContext) { encryptAndSignData(vaultKey.key) }

        return CreateVaultOutput(
            request = EncryptedCreateVault(
                addressId = userAddress.addressId.id,
                content = b64(encryptedVaultContents.array),
                contentFormatVersion = CONTENT_FORMAT_VERSION,
                encryptedVaultKey = b64(unarmor(encryptedVaultKey))

            ),
            shareKey = vaultKey
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}

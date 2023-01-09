package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.crypto.api.usecases.ReadKey
import me.proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.android.pass.data.impl.responses.ShareResponse
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.key.SigningKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.domain.key.usePrivateKey
import javax.inject.Inject

interface ReencryptShareEntityContents {
    suspend operator fun invoke(
        userAddress: UserAddress,
        response: ShareResponse,
        entity: ShareEntity
    ): Result<ShareEntity>
}

class ReencryptShareEntityContentsImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val vaultKeyRepository: VaultKeyRepository,
    private val readKey: ReadKey
) : ReencryptShareEntityContents {
    override suspend fun invoke(
        userAddress: UserAddress,
        response: ShareResponse,
        entity: ShareEntity
    ): Result<ShareEntity> {
        val signingKey = SigningKey(readKey(response.signingKey, isPrimary = true))
        return vaultKeyRepository.getVaultKeys(userAddress, ShareId(response.shareId), signingKey)
            .map { vaultKeys ->
                entity.copy(
                    keystoreEncryptedContent = reencryptContent(response, vaultKeys),
                    keystoreEncryptedPassphrase = reencryptSigningKeyPassphrase(
                        response.signingKeyPassphrase,
                        userAddress
                    )
                )
            }
    }


    private fun reencryptSigningKeyPassphrase(
        signingKeyPassphrase: String?,
        userAddress: UserAddress
    ): EncryptedByteArray? {
        if (signingKeyPassphrase == null) return null

        return userAddress.useKeys(cryptoContext) {
            val decrypted = decryptData(getArmored(getBase64Decoded(signingKeyPassphrase)))
            PlainByteArray(decrypted).encrypt(cryptoContext.keyStoreCrypto)
        }
    }

    private fun reencryptContent(response: ShareResponse, vaultKeys: List<VaultKey>): EncryptedByteArray? {
        if (response.content == null || response.contentRotationId == null) return null

        val key = vaultKeys.first { it.rotationId == response.contentRotationId }
        return key.usePrivateKey(cryptoContext) {
            val decrypted = decryptData(getArmored(getBase64Decoded(response.content)))
            PlainByteArray(decrypted).encrypt(cryptoContext.keyStoreCrypto)
        }
    }
}

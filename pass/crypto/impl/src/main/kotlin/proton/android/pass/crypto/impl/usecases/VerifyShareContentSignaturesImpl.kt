package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.error.CryptoException
import proton.android.pass.crypto.api.error.InvalidAddressSignature
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.EncryptedShareResponse
import proton.android.pass.crypto.api.usecases.VerifyShareContentSignatures
import proton.android.pass.log.api.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import proton.pass.domain.ShareType
import proton.pass.domain.key.VaultKey
import proton.pass.domain.key.publicKey
import proton.pass.domain.key.usePrivateKey
import javax.inject.Inject

class VerifyShareContentSignaturesImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : VerifyShareContentSignatures {

    @Suppress("ThrowsCount")
    override fun invoke(
        response: EncryptedShareResponse,
        contentSignatureKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>
    ) {
        // Check share contents
        if (response.targetType == ShareType.Item.value) {
            return
        }

        val contentRotationId = response.contentRotationId
            ?: throw CryptoException("Share should contain contentRotationId")
        val contentEncryptedAddressSignature = response.contentEncryptedAddressSignature
            ?: throw CryptoException("Share should contain contentEncryptedAddressSignature")
        val contentEncryptedVaultSignature = response.contentEncryptedVaultSignature
            ?: throw CryptoException("Share should contain contentEncryptedVaultSignature")
        val content = response.content ?: throw CryptoException("Share should contain content")

        // Obtain the vault key
        val vaultKey = vaultKeys.find {
            it.rotationId == contentRotationId
        }

        if (vaultKey == null) {
            val e = KeyNotFound("VaultKey not found")
            PassLogger.e(
                TAG,
                e,
                "VaultKey not found when opening share [shareId=${response.shareId}]" +
                    "[vaultKey.contentRotationId=${response.contentRotationId}]"
            )
            throw e
        }

        // Decrypt the signatures
        val decryptWithVaultKey = { data: String ->
            vaultKey.usePrivateKey(cryptoContext) {
                decryptData(getArmored(getBase64Decoded(data)))
            }
        }

        val addressSignature = decryptWithVaultKey(contentEncryptedAddressSignature)
        val vaultSignature = decryptWithVaultKey(contentEncryptedVaultSignature)
        val decryptedContent = decryptWithVaultKey(content)

        // Verify address signature
        val armoredAddressSignature =
            cryptoContext.pgpCrypto.getArmored(addressSignature, PGPHeader.Signature)
        val addressSignatureValid = contentSignatureKeys.any {
            cryptoContext.pgpCrypto.verifyData(decryptedContent, armoredAddressSignature, it.key)
        }

        if (!addressSignatureValid) {
            val e = InvalidAddressSignature()
            PassLogger.e(TAG, e, "Address signature not valid [shareId=${response.shareId}]")
            throw e
        }


        // Verify vault signature
        val vaultSignatureValid = cryptoContext.pgpCrypto.verifyData(
            data = decryptedContent,
            signature = cryptoContext.pgpCrypto.getArmored(vaultSignature, PGPHeader.Signature),
            publicKey = vaultKey.publicKey(cryptoContext).key
        )
        if (!vaultSignatureValid) {
            val e = InvalidSignature("Vault signature is not valid")
            PassLogger.e(TAG, e, "Vault signature is not valid [shareId=${response.shareId}]")
            throw e
        }
    }

    companion object {
        private const val TAG = "VerifyShareContentSignaturesImpl"
    }
}

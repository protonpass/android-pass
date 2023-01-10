package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.VerificationTime
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.error.CryptoException
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.EncryptedVaultItemKeyResponse
import proton.android.pass.crypto.api.usecases.OpenKeys
import proton.android.pass.crypto.api.usecases.VaultKeyList
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey
import proton.pass.domain.key.publicKey
import proton.pass.domain.key.usePrivateKey
import javax.inject.Inject

class OpenKeysImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : OpenKeys, BaseCryptoOperation(cryptoContext) {

    override fun open(
        keys: EncryptedVaultItemKeyResponse,
        signingKey: SigningKey,
        userAddress: UserAddress
    ): VaultKeyList {
        val maxRotationId = keys.vaultKeys.maxByOrNull { it.rotation }?.rotationId ?: ""
        val signingKeyPublicKey = signingKey.publicKey(cryptoContext)
        val convertedVaultKeys = keys.vaultKeys.map { vaultKey ->
            if (!validateKey(signingKeyPublicKey, vaultKey.key, vaultKey.keySignature)) {
                val e =
                    CryptoException("Key signature did not match [VaultKey.RotationID=${vaultKey.rotationId}]")
                PassLogger.w(
                    TAG,
                    e,
                    "Error validating vaultKey [vaultKey.rotationId=${vaultKey.rotationId}]" +
                        " [signingKey=${signingKey.keyId.id}]"
                )
                throw e
            }
            val passphrase = decryptVaultKeyPassphrase(vaultKey.keyPassphrase, userAddress)
            val isPrimary = vaultKey.rotationId == maxRotationId
            VaultKey(
                vaultKey.rotationId,
                vaultKey.rotation,
                Utils.readKey(vaultKey.key, isPrimary = isPrimary, passphrase = passphrase),
                passphrase
            )
        }

        val convertedItemKeys = keys.itemKeys.map { itemKey ->
            if (!validateKey(signingKeyPublicKey, itemKey.key, itemKey.keySignature)) {
                val e =
                    CryptoException("Key signature did not match [ItemKey.RotationID=${itemKey.rotationId}]")
                PassLogger.w(
                    TAG,
                    e,
                    "Error validating ItemKey [itemKey.rotationId=${itemKey.rotationId}] " +
                        "[signingKey=${signingKey.keyId.id}]"
                )
                throw e
            }

            val vaultKey = convertedVaultKeys.find {
                it.rotationId == itemKey.rotationId
            }

            if (vaultKey == null) {
                val e = KeyNotFound("Cannot find VaultKey")
                PassLogger.w(
                    TAG,
                    e,
                    "Cannot find VaultKey [vaultKey.rotationId=${itemKey.rotationId}]"
                )
                throw e
            }

            val passphrase = decryptItemKeyPassphrase(itemKey.keyPassphrase, vaultKey)
            val isPrimary = itemKey.rotationId == maxRotationId
            ItemKey(
                itemKey.rotationId,
                Utils.readKey(itemKey.key, isPrimary = isPrimary, passphrase = passphrase),
                passphrase
            )
        }

        return VaultKeyList(convertedVaultKeys, convertedItemKeys)
    }

    private fun decryptVaultKeyPassphrase(
        passphrase: String?,
        userAddress: UserAddress
    ): EncryptedByteArray? =
        passphrase?.let {
            userAddress.useKeys(cryptoContext) {
                val decryptedPassphrase = decryptData(getArmored(getBase64Decoded(it)))
                val asPlainByteArray = PlainByteArray(decryptedPassphrase)
                asPlainByteArray.use { cryptoContext.keyStoreCrypto.encrypt(it) }
            }
        }

    private fun decryptItemKeyPassphrase(
        passphrase: String?,
        vaultKey: VaultKey
    ): EncryptedByteArray? =
        passphrase?.let {
            vaultKey.usePrivateKey(cryptoContext) {
                val decryptedPassphrase = decryptData(getArmored(getBase64Decoded(it)))
                val asPlainByteArray = PlainByteArray(decryptedPassphrase)
                asPlainByteArray.use { cryptoContext.keyStoreCrypto.encrypt(it) }
            }
        }

    private fun validateKey(signingKey: PublicKey, key: String, keySignature: String): Boolean {
        val fingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, key)
        val armoredSignature = cryptoContext.pgpCrypto.getArmored(
            b64Decode(keySignature),
            PGPHeader.Signature
        )
        return cryptoContext.pgpCrypto.verifyData(
            fingerprint.encodeToByteArray(),
            armoredSignature,
            signingKey.key,
            time = VerificationTime.Now
        )
    }

    companion object {
        private const val TAG = "OpenKeys"
    }
}

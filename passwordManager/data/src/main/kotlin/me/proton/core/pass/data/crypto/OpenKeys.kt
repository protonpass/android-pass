package me.proton.core.pass.data.crypto

import javax.inject.Inject
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
import me.proton.core.pass.data.remote.VaultItemKeyResponseList
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.SigningKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.pass.domain.key.publicKey
import me.proton.core.pass.domain.key.usePrivateKey
import me.proton.core.pass.domain.repositories.VaultItemKeyList
import me.proton.core.user.domain.entity.UserAddress

class OpenKeys @Inject constructor(
    private val cryptoContext: CryptoContext
) {
    fun openKeys(
        keys: VaultItemKeyResponseList,
        signingKey: SigningKey,
        userAddress: UserAddress
    ): VaultItemKeyList {
        val maxRotationId = keys.vaultKeys.maxByOrNull { it.rotation }?.rotationId ?: ""
        val signingKeyPublicKey = signingKey.publicKey(cryptoContext)
        val convertedVaultKeys = keys.vaultKeys.map { vaultKey ->
            if (!validateKey(signingKeyPublicKey, vaultKey.key, vaultKey.keySignature)) {
                throw Exception("Key signature did not match [VaultKey.RotationID=${vaultKey.rotationId}]")
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
                throw Exception("Key signature did not match [ItemKey.RotationID=${itemKey.rotationId}]")
            }

            val vaultKey = convertedVaultKeys.find { it.rotationId == itemKey.rotationId } ?: throw Exception("Cannot find VaultKey with RotationID=${itemKey.rotationId}")
            val passphrase = decryptItemKeyPassphrase(itemKey.keyPassphrase, vaultKey)
            val isPrimary = itemKey.rotationId == maxRotationId
            ItemKey(
                itemKey.rotationId,
                Utils.readKey(itemKey.key, isPrimary = isPrimary, passphrase = passphrase),
                passphrase
            )
        }

        return VaultItemKeyList(convertedVaultKeys, convertedItemKeys)
    }

    private fun decryptVaultKeyPassphrase(passphrase: String?, userAddress: UserAddress): EncryptedByteArray? =
        passphrase?.let {
            userAddress.useKeys(cryptoContext) {
                val decryptedPassphrase = decryptData(getArmored(getBase64Decoded(it)))
                val asPlainByteArray = PlainByteArray(decryptedPassphrase)
                asPlainByteArray.use { cryptoContext.keyStoreCrypto.encrypt(it) }
            }
        }

    private fun decryptItemKeyPassphrase(passphrase: String?, vaultKey: VaultKey): EncryptedByteArray? =
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
            cryptoContext.pgpCrypto.getBase64Decoded(keySignature),
            PGPHeader.Signature
        )
        return cryptoContext.pgpCrypto.verifyData(
            fingerprint.encodeToByteArray(),
            armoredSignature,
            signingKey.key,
            time = VerificationTime.Now
        )
    }
}

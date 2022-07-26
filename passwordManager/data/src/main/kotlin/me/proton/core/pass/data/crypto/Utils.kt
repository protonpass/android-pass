package me.proton.core.pass.data.crypto

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.*
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.publicKey

object Utils {
    const val PASSPHRASE_LENGTH = 32

    fun generatePassphrase() = getRandomString(PASSPHRASE_LENGTH)

    fun getPrimaryV5Fingerprint(cryptoContext: CryptoContext, key: Armored): String {
        val fingerprints = cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(key)
        val decodedFingerprints = Json.decodeFromString<List<String>>(fingerprints)
        if (decodedFingerprints.isEmpty()) {
            throw Exception("Key does not contain any fingerprint")
        }
        return decodedFingerprints.first()
    }

    fun readKey(
        key: Armored,
        isPrimary: Boolean = false,
        isActive: Boolean = true,
        canEncrypt: Boolean = true,
        canVerify: Boolean = true,
        passphrase: EncryptedByteArray? = null
    ): ArmoredKey {
        if (key.contains("PGP PUBLIC KEY")) {
            return ArmoredKey.Public(key, PublicKey(key, isPrimary, isActive, canEncrypt, canVerify))
        } else if (key.contains("PGP PRIVATE KEY")) {
            return ArmoredKey.Private(key, PrivateKey(key, isPrimary, isActive, canEncrypt, canVerify, passphrase))
        }
        throw Exception("Could not detect the kind of key")
    }

    fun getPublicKey(context: CryptoContext, privateKey: PrivateKey): PublicKey =
        PublicKey(
            key = context.pgpCrypto.getPublicKey(privateKey.key),
            isPrimary = privateKey.isPrimary,
            isActive = privateKey.isActive,
            canEncrypt = privateKey.canEncrypt,
            canVerify = privateKey.canVerify
        )

    fun <R> usingPrivateKey(context: CryptoContext, key: ArmoredKey, block: (KeyHolderContext) -> R) =
        when (key) {
            is ArmoredKey.Public -> throw Exception("Cannot use public key as private key")
            is ArmoredKey.Private ->
                KeyHolderContext(
                    context,
                    PrivateKeyRing(context, listOf(key.key)),
                    PublicKeyRing(listOf(key.key.publicKey(context)))
                ).use { block(it) }
        }

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

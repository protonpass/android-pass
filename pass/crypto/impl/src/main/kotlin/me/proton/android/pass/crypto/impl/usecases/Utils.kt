package me.proton.android.pass.crypto.impl.usecases

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey

object Utils {
    private const val PASSPHRASE_LENGTH = 32

    fun generatePassphrase() = getRandomString(PASSPHRASE_LENGTH)

    fun getPrimaryV5Fingerprint(cryptoContext: CryptoContext, key: Armored): String {
        val fingerprints = cryptoContext.pgpCrypto.getJsonSHA256Fingerprints(key)
        val decodedFingerprints = Json.decodeFromString<List<String>>(fingerprints)
        require(decodedFingerprints.isNotEmpty())
        return decodedFingerprints.first()
    }

    @Suppress("TooGenericExceptionThrown")
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

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}


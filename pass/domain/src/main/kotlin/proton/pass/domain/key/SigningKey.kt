package proton.pass.domain.key

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PrivateKeyRing
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey
import me.proton.core.key.domain.publicKey

data class SigningKey(
    val key: ArmoredKey
) : KeyHolderPrivateKey {
    override val keyId: KeyId
        get() = KeyId("SigningKey")

    @Suppress("TooGenericExceptionThrown")
    override val privateKey: PrivateKey
        get() = when (key) {
            is ArmoredKey.Public -> throw Exception("Cannot use Public key as Private key")
            is ArmoredKey.Private -> key.key
        }
}

fun SigningKey.publicKey(cryptoContext: CryptoContext): PublicKey =
    when (key) {
        is ArmoredKey.Public -> key.key
        is ArmoredKey.Private -> PublicKey(
            key = cryptoContext.pgpCrypto.getPublicKey(key.key.key),
            isPrimary = key.key.isPrimary,
            isActive = key.key.isActive,
            canEncrypt = key.key.canEncrypt,
            canVerify = key.key.canVerify
        )
    }

@Suppress("TooGenericExceptionThrown")
inline fun <R> SigningKey.usePrivateKey(
    context: CryptoContext,
    block: KeyHolderContext.() -> R
): R =
    when (key) {
        is ArmoredKey.Public -> throw Exception("Cannot use public key as private key")
        is ArmoredKey.Private ->
            KeyHolderContext(
                context,
                PrivateKeyRing(context, listOf(key.key)),
                PublicKeyRing(listOf(key.key.publicKey(context)))
            ).use { block(it) }
    }

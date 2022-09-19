package me.proton.core.pass.data.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored

@Suppress("UnnecessaryAbstractClass")
abstract class BaseCryptoOperation constructor(
    private val cryptoContext: CryptoContext
) {
    fun b64(data: ByteArray) = cryptoContext.pgpCrypto.getBase64Encoded(data)
    fun b64Decode(source: String) = cryptoContext.pgpCrypto.getBase64Decoded(source)
    fun unarmor(data: Armored) = cryptoContext.pgpCrypto.getUnarmored(data)
}

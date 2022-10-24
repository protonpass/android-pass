package me.proton.pass.data.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.publicKey

fun ArmoredKey.publicKey(cryptoContext: CryptoContext): PublicKey = when (this) {
    is ArmoredKey.Public -> key
    is ArmoredKey.Private -> key.publicKey(cryptoContext)
}

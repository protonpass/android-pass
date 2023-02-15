package proton.pass.domain.key

import me.proton.core.crypto.common.keystore.EncryptedByteArray

data class ItemKey(
    val rotation: Long,
    val key: EncryptedByteArray,
    val responseKey: String
)

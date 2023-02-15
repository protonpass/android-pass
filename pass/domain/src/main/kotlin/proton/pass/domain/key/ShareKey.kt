package proton.pass.domain.key

import me.proton.core.crypto.common.keystore.EncryptedByteArray

data class ShareKey(
    val rotation: Long,
    val key: EncryptedByteArray,
    val responseKey: String,
    val createTime: Long
)

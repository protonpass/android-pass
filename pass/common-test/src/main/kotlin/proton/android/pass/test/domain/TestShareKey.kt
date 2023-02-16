package proton.android.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.pass.domain.key.ShareKey

@Suppress("UnderscoresInNumericLiterals")
object TestShareKey {
    fun createPrivate(): ShareKey = ShareKey(
        rotation = 1,
        key = EncryptedByteArray(byteArrayOf(1, 2, 3)),
        responseKey = "base64ShareKey",
        createTime = 12345678
    )
}

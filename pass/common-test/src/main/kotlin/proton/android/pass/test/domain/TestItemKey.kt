package proton.android.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.pass.domain.key.ItemKey

object TestItemKey {
    fun createPrivate(): ItemKey {
        return ItemKey(
            rotation = 1,
            key = EncryptedByteArray(byteArrayOf(1, 2, 3)),
            responseKey = "somebase64key"
        )
    }
}

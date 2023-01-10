package proton.android.pass.test.domain

import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import proton.pass.domain.key.ItemKey

object TestItemKey {
    fun createPrivate(): ItemKey {
        val armored = "armoredKey"
        return ItemKey(
            rotationId = "test-rotation",
            key = ArmoredKey.Private(
                armored = armored,
                key = PrivateKey(
                    key = armored,
                    isPrimary = true,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = true,
                    passphrase = null
                )
            ),
            encryptedKeyPassphrase = null
        )
    }
}

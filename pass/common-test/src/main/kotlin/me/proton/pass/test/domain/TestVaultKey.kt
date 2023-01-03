package me.proton.pass.test.domain

import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.pass.domain.key.VaultKey

object TestVaultKey {
    fun createPrivate(): VaultKey {
        val armored = "armoredKey"
        return VaultKey(
            rotationId = "test-rotation",
            rotation = 1,
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

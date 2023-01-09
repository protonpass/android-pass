package me.proton.android.pass.crypto.fakes.usecases

import me.proton.android.pass.crypto.api.usecases.ReadKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.ArmoredKey

class TestReadKey(private val response: ArmoredKey) : ReadKey {
    override fun invoke(
        key: Armored,
        isPrimary: Boolean,
        isActive: Boolean,
        canEncrypt: Boolean,
        canVerify: Boolean,
        passphrase: EncryptedByteArray?
    ): ArmoredKey = response
}

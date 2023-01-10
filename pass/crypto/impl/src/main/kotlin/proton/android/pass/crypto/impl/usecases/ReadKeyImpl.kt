package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.usecases.ReadKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.ArmoredKey
import javax.inject.Inject

class ReadKeyImpl @Inject constructor() : ReadKey {
    override fun invoke(
        key: Armored,
        isPrimary: Boolean,
        isActive: Boolean,
        canEncrypt: Boolean,
        canVerify: Boolean,
        passphrase: EncryptedByteArray?
    ): ArmoredKey = Utils.readKey(
        key = key,
        isPrimary = isPrimary,
        isActive = isActive,
        canEncrypt = canEncrypt,
        canVerify = canVerify,
        passphrase = passphrase
    )
}

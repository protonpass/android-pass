package proton.android.pass.crypto.api.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.ArmoredKey

interface ReadKey {
    operator fun invoke(
        key: Armored,
        isPrimary: Boolean = false,
        isActive: Boolean = true,
        canEncrypt: Boolean = true,
        canVerify: Boolean = true,
        passphrase: EncryptedByteArray? = null
    ): ArmoredKey
}

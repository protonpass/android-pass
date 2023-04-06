package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.error.InvalidSignature
import javax.inject.Inject

interface ReencryptShareKey {
    operator fun invoke(
        encryptionContext: EncryptionContext,
        user: User,
        key: String
    ): EncryptedByteArray
}

class ReencryptShareKeyImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : ReencryptShareKey {
    override fun invoke(
        encryptionContext: EncryptionContext,
        user: User,
        key: String
    ): EncryptedByteArray {
        val decodedKey = Base64.decodeBase64(key)
        val decrypted = user.useKeys(cryptoContext) {
            decryptAndVerifyData(getArmored(decodedKey))
        }

        if (decrypted.status != VerificationStatus.Success) {
            throw InvalidSignature("ShareKey signature did not match")
        }

        return encryptionContext.encrypt(decrypted.data)
    }
}

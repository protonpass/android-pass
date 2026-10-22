/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */


package proton.android.pass.crypto.impl

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.decryptData
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.verifyData
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.UserKeys
import proton.android.pass.crypto.impl.extensions.tryUseKeys

internal class CoreUserKeys(
    private val user: User,
    private val cryptoContext: CryptoContext
) : UserKeys {

    override fun hasActiveKey(keyId: String): Boolean = user.keys.any {
        it.active == true && keyId == it.keyId.id
    }

    override fun primaryKeyId(): String? = user.keys.primary()?.keyId?.id

    override fun decryptAndVerify(message: ByteArray, label: String): ByteArray {
        val decrypted = user.tryUseKeys(label, cryptoContext) {
            decryptAndVerifyData(getArmored(message))
        }

        if (decrypted.status != VerificationStatus.Success) {
            throw InvalidSignature("ShareKey signature did not match")
        }

        return decrypted.data
    }

    override fun decrypt(token: String, label: String): ByteArray = user.tryUseKeys(
        message = label,
        cryptoContext = cryptoContext
    ) {
        decryptData(token)
    }

    override fun decryptAndVerifyDetached(
        token: String,
        signature: String,
        signatureContext: String,
        label: String
    ): ByteArray = user.tryUseKeys(
        message = label,
        cryptoContext = cryptoContext
    ) {
        val decryptedToken = decryptData(token)
        val isVerified = verifyData(
            data = decryptedToken,
            signature = signature,
            verificationContext = VerificationContext(
                value = signatureContext,
                required = VerificationContext.ContextRequirement.Required.Always
            )
        )
        if (!isVerified) {
            throw InvalidSignature("Invite signature did not match")
        }
        decryptedToken
    }

    override fun encryptAndSign(data: ByteArray, label: String): String = user.tryUseKeys(
        message = label,
        cryptoContext = cryptoContext
    ) {
        encryptAndSignData(data)
    }
}

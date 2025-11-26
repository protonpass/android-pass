/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.data.impl.extensions.tryUseKeys
import javax.inject.Inject

sealed interface ReencryptKeyInput

data class ReencryptShareKeyInput(
    val key: String,
    val userKeyId: String,
    val addressId: String
) : ReencryptKeyInput

data class ReencryptGroupKeyInput(
    val key: String,
    val publicKeyRing: PublicKeyRing,
    val invitedAddress: UserAddress
) : ReencryptKeyInput

interface ReencryptShareKey {
    operator fun invoke(
        encryptionContext: EncryptionContext,
        user: User,
        input: ReencryptKeyInput
    ): EncryptedByteArray
}

class ReencryptShareKeyImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : ReencryptShareKey {
    override fun invoke(
        encryptionContext: EncryptionContext,
        user: User,
        input: ReencryptKeyInput
    ): EncryptedByteArray = when (input) {
        is ReencryptGroupKeyInput -> reencryptGroupKey(encryptionContext, input)
        is ReencryptShareKeyInput -> reencryptShareKey(encryptionContext, user, input)
    }

    private fun reencryptShareKey(
        encryptionContext: EncryptionContext,
        user: User,
        input: ReencryptShareKeyInput
    ): EncryptedByteArray {
        val hasUserKey = user.keys.any {
            it.active == true && input.userKeyId == it.keyId.id
        }

        if (!hasUserKey) {
            throw UserKeyNotActive()
        }

        val decodedKey = Base64.decodeBase64(input.key)

        val decrypted = user.tryUseKeys("reencrypt share key", cryptoContext) {
            decryptAndVerifyData(getArmored(decodedKey))
        }

        if (decrypted.status != VerificationStatus.Success) {
            throw InvalidSignature("ShareKey signature did not match")
        }

        return encryptionContext.encrypt(decrypted.data)
    }

    fun reencryptGroupKey(encryptionContext: EncryptionContext, input: ReencryptGroupKeyInput): EncryptedByteArray {
        val decryptedShareKey = input.invitedAddress.useKeys(cryptoContext) {
            decryptAndVerifyData(
                message = getArmored(Base64.decodeBase64(input.key)),
                verifyKeyRing = input.publicKeyRing
            )
        }
        if (decryptedShareKey.status != VerificationStatus.Success) {
            throw InvalidSignature("ShareKey signature did not match")
        }
        return encryptionContext.encrypt(decryptedShareKey.data)
    }
}

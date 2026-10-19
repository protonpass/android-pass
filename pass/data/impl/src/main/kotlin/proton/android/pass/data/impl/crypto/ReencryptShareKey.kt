/*
 * Copyright (c) 2023-2026 Proton AG
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
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.data.impl.exception.UserKeyNotActive
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

private const val TAG = "ReencryptShareKey"

sealed interface ReencryptKeyInput

data class ReencryptShareKeyInput(
    val key: String,
    val userKeyId: String,
    val addressId: String,
    val userKeys: UserKeys
) : ReencryptKeyInput

data class ReencryptGroupKeyInput(
    val key: String,
    val publicKeyRing: PublicKeyRing,
    val invitedAddress: UserAddress
) : ReencryptKeyInput

interface ReencryptShareKey {
    operator fun invoke(encryptionContext: EncryptionContext, input: ReencryptKeyInput): EncryptedByteArray
}

class ReencryptShareKeyImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : ReencryptShareKey {
    override fun invoke(encryptionContext: EncryptionContext, input: ReencryptKeyInput): EncryptedByteArray =
        when (input) {
            is ReencryptGroupKeyInput -> reencryptGroupKey(encryptionContext, input)
            is ReencryptShareKeyInput -> reencryptShareKey(encryptionContext, input)
        }

    private fun reencryptShareKey(
        encryptionContext: EncryptionContext,
        input: ReencryptShareKeyInput
    ): EncryptedByteArray {
        if (!input.userKeys.hasActiveKey(input.userKeyId)) {
            PassLogger.w(TAG, "reencryptShareKey: userKeyId did not match any locally active key")
            throw UserKeyNotActive()
        }

        val decrypted = input.userKeys.decryptAndVerify(Base64.decodeBase64(input.key))

        return encryptionContext.encrypt(decrypted)
    }

    private fun reencryptGroupKey(
        encryptionContext: EncryptionContext,
        input: ReencryptGroupKeyInput
    ): EncryptedByteArray {
        val decryptedShareKey = input.invitedAddress.useKeys(cryptoContext) {
            decryptAndVerifyData(
                message = getArmored(Base64.decodeBase64(input.key)),
                verifyKeyRing = input.publicKeyRing
            )
        }
        if (decryptedShareKey.status != VerificationStatus.Success) {
            PassLogger.w(TAG, "reencryptGroupKey: signature verification failed")
            throw InvalidSignature("ShareKey signature did not match")
        }
        return encryptionContext.encrypt(decryptedShareKey.data)
    }
}

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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.usecases.GroupKeyDecryptor
import proton.android.pass.crypto.api.usecases.UserKeys
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
    val decryptor: GroupKeyDecryptor
) : ReencryptKeyInput

interface ReencryptShareKey {
    operator fun invoke(encryptionContext: EncryptionContext, input: ReencryptKeyInput): EncryptedByteArray
}

class ReencryptShareKeyImpl @Inject constructor() : ReencryptShareKey {
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

        val decrypted = input.userKeys.decryptAndVerify(
            message = Base64.decodeBase64(input.key),
            label = "reencrypt share key"
        )

        return encryptionContext.encrypt(decrypted)
    }

    private fun reencryptGroupKey(
        encryptionContext: EncryptionContext,
        input: ReencryptGroupKeyInput
    ): EncryptedByteArray {
        val decrypted = input.decryptor.decryptAndVerify(Base64.decodeBase64(input.key))
        return encryptionContext.encrypt(decrypted)
    }
}

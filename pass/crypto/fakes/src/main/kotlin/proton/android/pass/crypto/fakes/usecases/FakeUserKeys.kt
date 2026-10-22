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


package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.UserKeys

class FakeUserKeys(
    private val activeKeyIds: Set<String> = emptySet(),
    private val primaryKeyId: String? = null
) : UserKeys {

    private val recordedMessages = mutableListOf<ByteArray>()
    private val recordedEncryptInputs = mutableListOf<ByteArray>()
    private val recordedLabels = mutableListOf<String>()

    var decryptAndVerifyBehavior: (ByteArray) -> ByteArray = { DEFAULT_DECRYPTED }
    var decryptBehavior: (String) -> ByteArray = { DEFAULT_DECRYPTED }
    var decryptAndVerifyDetachedBehavior: (String, String, String) -> ByteArray = { _, _, _ -> DEFAULT_DECRYPTED }
    var encryptAndSignBehavior: (ByteArray) -> String = { DEFAULT_ENCRYPTED }

    val decryptedMessages: List<ByteArray> get() = recordedMessages
    val encryptInputs: List<ByteArray> get() = recordedEncryptInputs
    val labels: List<String> get() = recordedLabels

    override fun hasActiveKey(keyId: String): Boolean = keyId in activeKeyIds

    override fun primaryKeyId(): String? = primaryKeyId

    override fun decryptAndVerify(message: ByteArray, label: String): ByteArray {
        recordedLabels.add(label)
        recordedMessages.add(message)
        return decryptAndVerifyBehavior(message)
    }

    override fun decrypt(token: String, label: String): ByteArray {
        recordedLabels.add(label)
        return decryptBehavior(token)
    }

    override fun decryptAndVerifyDetached(
        token: String,
        signature: String,
        signatureContext: String,
        label: String
    ): ByteArray {
        recordedLabels.add(label)
        return decryptAndVerifyDetachedBehavior(token, signature, signatureContext)
    }

    override fun encryptAndSign(data: ByteArray, label: String): String {
        recordedLabels.add(label)
        recordedEncryptInputs.add(data)
        return encryptAndSignBehavior(data)
    }

    companion object {
        val DEFAULT_DECRYPTED: ByteArray = byteArrayOf(7, 8)
        const val DEFAULT_ENCRYPTED = "encrypted"
    }
}

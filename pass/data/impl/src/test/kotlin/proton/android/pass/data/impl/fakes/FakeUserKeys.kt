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

package proton.android.pass.data.impl.fakes

import proton.android.pass.data.impl.crypto.UserKeys

class FakeUserKeys(
    private val activeKeyIds: Set<String> = emptySet()
) : UserKeys {

    private val recordedMessages = mutableListOf<ByteArray>()

    var decryptBehavior: (ByteArray) -> ByteArray = { DEFAULT_DECRYPTED }

    val decryptedMessages: List<ByteArray> get() = recordedMessages

    override fun hasActiveKey(keyId: String): Boolean = keyId in activeKeyIds

    override fun decryptAndVerify(message: ByteArray): ByteArray {
        recordedMessages.add(message)
        return decryptBehavior(message)
    }

    companion object {
        val DEFAULT_DECRYPTED: ByteArray = byteArrayOf(7, 8)
    }
}

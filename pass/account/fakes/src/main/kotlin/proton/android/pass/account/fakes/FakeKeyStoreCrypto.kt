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

package proton.android.pass.account.fakes

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import proton.android.pass.crypto.api.Base64

class FakeException(override val message: String) : RuntimeException(message)

object FakeKeyStoreCrypto : KeyStoreCrypto {
    private const val ENCRYPTED_SUFFIX = "-encrypted"
    private val ENCRYPTED_TRAIL = listOf(0xCA.toByte(), 0xFE.toByte())

    override fun isUsingKeyStore(): Boolean = true
    override fun encrypt(value: String): EncryptedString =
        Base64.encodeBase64String("${value}$ENCRYPTED_SUFFIX".encodeToByteArray())
    override fun encrypt(value: PlainByteArray): EncryptedByteArray {
        val cloned = value.array.clone().toMutableList()
        ENCRYPTED_TRAIL.forEach {
            cloned.add(it)
        }
        return EncryptedByteArray(cloned.toByteArray())
    }

    override fun decrypt(value: EncryptedString): String {
        val decoded = String(Base64.decodeBase64(value))
        if (!decoded.endsWith(ENCRYPTED_SUFFIX)) {
            throw FakeException("Cannot decrypt. String does not contain the expected suffix")
        }
        return decoded.replace(ENCRYPTED_SUFFIX, "")
    }

    override fun decrypt(value: EncryptedByteArray): PlainByteArray {
        if (value.array.size < ENCRYPTED_TRAIL.size) {
            throw FakeException("Cannot decrypt. Array does not end with the expected trail")
        }
        val startTrailIndex = value.array.size - ENCRYPTED_TRAIL.size
        ENCRYPTED_TRAIL.forEachIndexed { idx, byte ->
            val arrayIdx = idx + startTrailIndex
            if (value.array[arrayIdx] != byte) {
                throw FakeException("Cannot decrypt. Array does not contain the expected trail byte at index $arrayIdx")
            }
        }

        val newByteArray = ByteArray(value.array.size - ENCRYPTED_TRAIL.size)
        for (i in 0 until value.array.size - ENCRYPTED_TRAIL.size) {
            newByteArray[i] = value.array[i]
        }
        return PlainByteArray(newByteArray)
    }
}

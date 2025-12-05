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

package proton.android.pass.crypto.fakes.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionTag

class FakeException(override val message: String) : IllegalStateException(message)

object FakeEncryptionContext : EncryptionContext {
    private const val ENCRYPTED_SUFFIX = "-encrypted"
    private val ENCRYPTED_TRAIL = listOf(0xCA.toByte(), 0xFE.toByte())

    override fun encrypt(content: String): EncryptedString =
        Base64.encodeBase64String("${content}$ENCRYPTED_SUFFIX".encodeToByteArray())

    override fun encrypt(content: ByteArray, tag: EncryptionTag?): EncryptedByteArray {
        val cloned = content.clone().toMutableList()
        ENCRYPTED_TRAIL.forEach {
            cloned.add(it)
        }
        return EncryptedByteArray(cloned.toByteArray())
    }

    override fun decrypt(content: EncryptedString): String {
        val decoded = String(Base64.decodeBase64(content))
        if (!decoded.endsWith(ENCRYPTED_SUFFIX)) {
            throw FakeException("Cannot decrypt. String does not contain the expected suffix")
        }
        return decoded.replace(ENCRYPTED_SUFFIX, "")
    }

    override fun decrypt(content: EncryptedByteArray, tag: EncryptionTag?): ByteArray =
        runCatching { decode(content.array) }.fold(
            onSuccess = { it },
            onFailure = {
                // Check if the content is already decrypted
                val suffix = ENCRYPTED_SUFFIX.encodeToByteArray()
                val endsWithSuffix = content.array.takeLast(suffix.size) == suffix.toList()
                return if (endsWithSuffix) {
                    // It is an encrypted string. Convert it and run a decrypt(string)
                    decrypt(Base64.encodeBase64String(content.array)).encodeToByteArray()
                } else {
                    Base64.decodeBase64(content.array)
                }
            }
        )

    private fun decode(byteArray: ByteArray): ByteArray {
        if (byteArray.size < ENCRYPTED_TRAIL.size) {
            throw FakeException("Cannot decrypt. Array does not end with the expected trail")
        }
        val startTrailIndex = byteArray.size - ENCRYPTED_TRAIL.size
        ENCRYPTED_TRAIL.forEachIndexed { idx, byte ->
            val arrayIdx = idx + startTrailIndex
            if (byteArray[arrayIdx] != byte) {
                throw FakeException("Cannot decrypt. Array does not contain the expected trail byte at index $arrayIdx")
            }
        }

        val newByteArray = ByteArray(byteArray.size - ENCRYPTED_TRAIL.size)
        for (i in 0 until byteArray.size - ENCRYPTED_TRAIL.size) {
            newByteArray[i] = byteArray[i]
        }
        return newByteArray
    }
}


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

package proton.android.pass.crypto.api

import java.security.SecureRandom

data class EncryptionKey(private val key: ByteArray) {
    fun clear() {
        key.indices.map { idx ->
            key[idx] = 0x00.toByte()
        }
    }

    fun clone(): EncryptionKey = EncryptionKey(key.clone())

    fun value(): ByteArray {
        if (isEmpty()) {
            throw IllegalStateException("Key has been cleared")
        }
        return key
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionKey

        return key.contentEquals(other.key)
    }

    override fun hashCode(): Int = key.contentHashCode()

    private fun isEmpty(): Boolean = key.all { it == 0x00.toByte() }

    companion object {
        private const val keySize = 32

        fun generate(): EncryptionKey {
            val random = SecureRandom()
            val buff = ByteArray(keySize)
            random.nextBytes(buff)
            return EncryptionKey(buff)
        }
    }
}

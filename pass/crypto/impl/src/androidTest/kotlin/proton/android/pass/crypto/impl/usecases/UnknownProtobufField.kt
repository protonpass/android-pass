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

package proton.android.pass.crypto.impl.usecases

internal object UnknownProtobufField {

    private const val FIELD_NUMBER = 999
    private const val WIRE_TYPE_FIXED64 = 1
    private const val FIXED64_SIZE = 8

    val bytes: ByteArray = varint(FIELD_NUMBER shl 3 or WIRE_TYPE_FIXED64) +
        ByteArray(FIXED64_SIZE) { (it + 1).toByte() }

    fun isContainedIn(content: ByteArray): Boolean = content.toList().windowed(bytes.size).contains(bytes.toList())

    private fun varint(value: Int): ByteArray {
        val encoded = mutableListOf<Byte>()
        var remaining = value
        while (remaining >= 0x80) {
            encoded += (remaining and 0x7F or 0x80).toByte()
            remaining = remaining ushr 7
        }
        encoded += remaining.toByte()
        return encoded.toByteArray()
    }
}

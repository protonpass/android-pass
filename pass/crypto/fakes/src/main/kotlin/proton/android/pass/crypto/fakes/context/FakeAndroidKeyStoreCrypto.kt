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

package proton.android.pass.crypto.fakes.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray

class FakeAndroidKeyStoreCrypto : KeyStoreCrypto {

    var isKeyStoreAvailable: Boolean = true

    override fun isUsingKeyStore(): Boolean = isKeyStoreAvailable

    override fun encrypt(value: String): EncryptedString = value

    override fun decrypt(value: EncryptedString): String = value

    override fun encrypt(value: PlainByteArray): EncryptedByteArray = if (isKeyStoreAvailable) {
        EncryptedByteArray(ByteArray(IV_SIZE) + value.array + ByteArray(TAG_SIZE) { TAG_BYTE })
    } else {
        EncryptedByteArray(value.array.copyOf())
    }

    override fun decrypt(value: EncryptedByteArray): PlainByteArray = if (isKeyStoreAvailable) {
        val array = value.array
        check(array.size >= IV_SIZE + TAG_SIZE) { "Encrypted value too short" }
        check(array.takeLast(TAG_SIZE).all { it == TAG_BYTE }) { "Invalid tag" }
        PlainByteArray(array.copyOfRange(IV_SIZE, array.size - TAG_SIZE))
    } else {
        PlainByteArray(value.array.copyOf())
    }

    private companion object {
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 16
        private const val TAG_BYTE = 0x7A.toByte()
    }
}

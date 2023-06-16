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

package proton.android.pass.crypto.impl

import org.junit.Test
import proton.android.pass.crypto.api.EncryptionKey

class EncryptionKeyTest {

    @Test
    fun `key has the expected key size`() {
        val key = EncryptionKey.generate()

        assert(key.value().size == 32)
    }

    @Test
    fun `key contains different values`() {
        val key = EncryptionKey.generate()

        assert(key.value().any { it != 0x00.toByte() })
    }

    @Test
    fun `generates two different keys when invoked twice`() {
        val key1 = EncryptionKey.generate()
        val key2 = EncryptionKey.generate()

        assert(key1 != key2)
    }

    @Test(expected = IllegalStateException::class)
    fun `clears the key when clear is called`() {
        val key = EncryptionKey.generate()
        key.clear()

        assert(key.value().all { it == 0x00.toByte() })
    }

}

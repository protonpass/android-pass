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

package proton.android.pass.crypto.impl.context

import org.junit.Test
import proton.android.pass.crypto.api.EncryptionKey
import kotlin.test.assertEquals

class EncryptionContextImplTest {

    @Test
    fun isAbleToEncryptAndDecryptString() {
        val context = EncryptionContextImpl(provideKey())
        val content = "abcàèìò+a+e+i+o¡¿✅"

        val encrypted = context.encrypt(content)
        val decrypted = context.decrypt(encrypted)

        assertEquals(content, decrypted)
    }

    @Test
    fun isAbleToEncryptAndDecryptByteArray() {
        val context = EncryptionContextImpl(provideKey())
        val content = byteArrayOf(0xca.toByte(), 0xfe.toByte())

        val encrypted = context.encrypt(content)
        val decrypted = context.decrypt(encrypted)

        assertEquals(decrypted.size, content.size)
        content.indices.forEach { idx ->
            assertEquals(decrypted[idx], content[idx])
        }
    }

    fun provideKey(): EncryptionKey = EncryptionKey(
        ByteArray(
            32,
            init = { 0xab.toByte() })
    )

}


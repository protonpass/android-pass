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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.error.BadTagException
import kotlin.test.assertContentEquals
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

    @Test
    fun isAbleToEncryptAndDecryptWithTag() {
        val content = "some content"
        val context = EncryptionContextImpl(provideKey())

        val encrypted = context.encrypt(content.encodeToByteArray(), EncryptionTag.ItemKey)
        val decrypted = context.decrypt(encrypted, EncryptionTag.ItemKey)

        assertContentEquals(content.encodeToByteArray(), decrypted)
        assertEquals(content, decrypted.decodeToString())
    }

    @Test
    fun isAbleToDecryptFromRust() {
        val base64Key = "Kl6JlvgEOd4wyOgGKpTKXydEgh799C/gD2+YcmrlUns="
        val base64Content = "94UM35oCUdc01Rpn6GWLpVyE2IIbgtajF/V986H9tjyC/hWlbjS/"
        val content = Base64.decodeBase64(base64Content)

        val context = EncryptionContextImpl(EncryptionKey(Base64.decodeBase64(base64Key)))
        val decrypted = context.decrypt(EncryptedByteArray(content), EncryptionTag.ItemContent)
        val decryptedAsString = String(decrypted, Charsets.US_ASCII)
        assertEquals("somecontent", decryptedAsString)
    }

    @Test
    fun failsIfWrongTagIsUsed() {
        val content = "some content"
        val context = EncryptionContextImpl(provideKey())

        val encrypted = context.encrypt(content.encodeToByteArray(), EncryptionTag.VaultContent)

        assertThrows(BadTagException::class.java) {
            context.decrypt(encrypted, EncryptionTag.ItemKey)
        }
    }

    private fun provideKey(): EncryptionKey = EncryptionKey(
        ByteArray(
            32,
            init = { 0xab.toByte() }
        )
    )

}


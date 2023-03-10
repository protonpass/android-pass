package proton.android.pass.crypto.impl.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.apache.commons.codec.binary.Base64
import org.junit.Assert.assertThrows
import org.junit.Test
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.error.BadTagException
import proton.android.pass.crypto.api.EncryptionKey
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


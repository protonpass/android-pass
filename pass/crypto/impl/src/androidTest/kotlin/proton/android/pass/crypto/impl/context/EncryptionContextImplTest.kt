package proton.android.pass.crypto.impl.context

import org.junit.Test
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


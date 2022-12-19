package me.proton.android.pass.data.impl.crypto.context

import me.proton.android.pass.data.api.crypto.EncryptionKey
import org.junit.Test
import kotlin.test.assertEquals

class EncryptionContextImplTest {

    @Test
    fun isAbleToEncryptAndDecryptString() {
        val context = EncryptionContextImpl(provideKey())

        val encrypted = context.encrypt("abc")
        val decrypted = context.decrypt(encrypted)

        assertEquals("abc", decrypted)
    }

    fun provideKey(): EncryptionKey =
        EncryptionKey(ByteArray(32, init = { 0xab.toByte() }))

}

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

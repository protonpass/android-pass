package proton.android.pass.crypto.impl.context

import kotlin.random.Random

data class EncryptionKey(val key: ByteArray) {
    fun clear() {
        key.indices.map { idx ->
            key.set(idx, 0x00.toByte())
        }
    }

    companion object {
        private const val keySize = 32

        fun generate(): EncryptionKey = EncryptionKey(Random.Default.nextBytes(keySize))
    }
}

package proton.android.pass.crypto.api

import kotlin.random.Random

data class EncryptionKey(val key: ByteArray) {
    fun clear() {
        key.indices.map { idx ->
            key.set(idx, 0x00.toByte())
        }
    }

    fun clone(): EncryptionKey = EncryptionKey(key.clone())

    companion object {
        private const val keySize = 32

        fun generate(): EncryptionKey = EncryptionKey(Random.nextBytes(keySize))
    }
}

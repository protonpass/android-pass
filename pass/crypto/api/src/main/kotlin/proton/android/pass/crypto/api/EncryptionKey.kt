package proton.android.pass.crypto.api

import java.security.SecureRandom

data class EncryptionKey(val key: ByteArray) {
    fun clear() {
        key.indices.map { idx ->
            key.set(idx, 0x00.toByte())
        }
    }

    fun clone(): EncryptionKey = EncryptionKey(key.clone())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionKey

        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int = key.contentHashCode()

    companion object {
        private const val keySize = 32

        fun generate(): EncryptionKey {
            val random = SecureRandom()
            val buff = ByteArray(keySize)
            random.nextBytes(buff)
            return EncryptionKey(buff)
        }
    }
}

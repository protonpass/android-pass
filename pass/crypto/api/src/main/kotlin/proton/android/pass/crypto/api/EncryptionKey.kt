package proton.android.pass.crypto.api

import java.security.SecureRandom

data class EncryptionKey(private val key: ByteArray) {
    fun clear() {
        key.indices.map { idx ->
            key[idx] = 0x00.toByte()
        }
    }

    fun clone(): EncryptionKey = EncryptionKey(key.clone())

    fun value(): ByteArray {
        if (isEmpty()) {
            throw IllegalStateException("Key has been cleared")
        }
        return key
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionKey

        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int = key.contentHashCode()

    private fun isEmpty(): Boolean = key.all { it == 0x00.toByte() }

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

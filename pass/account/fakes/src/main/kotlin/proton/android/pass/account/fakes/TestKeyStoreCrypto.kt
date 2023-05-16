package proton.android.pass.account.fakes

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray

class TestException(override val message: String) : RuntimeException(message)

object TestKeyStoreCrypto : KeyStoreCrypto {
    private const val ENCRYPTED_SUFFIX = "-encrypted"
    private val ENCRYPTED_TRAIL = listOf(0xCA.toByte(), 0xFE.toByte())

    override fun isUsingKeyStore(): Boolean = true
    override fun encrypt(value: String): EncryptedString = "${value}$ENCRYPTED_SUFFIX"
    override fun encrypt(value: PlainByteArray): EncryptedByteArray {
        val cloned = value.array.clone().toMutableList()
        ENCRYPTED_TRAIL.forEach {
            cloned.add(it)
        }
        return EncryptedByteArray(cloned.toByteArray())
    }

    override fun decrypt(value: EncryptedString): String {
        if (!value.endsWith(ENCRYPTED_SUFFIX)) {
            throw TestException("Cannot decrypt. String does not contain the expected suffix")
        }
        return value.replace(ENCRYPTED_SUFFIX, "")
    }

    override fun decrypt(value: EncryptedByteArray): PlainByteArray {
        if (value.array.size < ENCRYPTED_TRAIL.size) {
            throw TestException("Cannot decrypt. Array does not end with the expected trail")
        }
        val startTrailIndex = value.array.size - ENCRYPTED_TRAIL.size
        ENCRYPTED_TRAIL.forEachIndexed { idx, byte ->
            val arrayIdx = idx + startTrailIndex
            if (value.array[arrayIdx] != byte) {
                throw TestException("Cannot decrypt. Array does not contain the expected trail byte at index $arrayIdx")
            }
        }

        val newByteArray = ByteArray(value.array.size - ENCRYPTED_TRAIL.size)
        for (i in 0 until value.array.size - ENCRYPTED_TRAIL.size) {
            newByteArray[i] = value.array[i]
        }
        return PlainByteArray(newByteArray)
    }
}

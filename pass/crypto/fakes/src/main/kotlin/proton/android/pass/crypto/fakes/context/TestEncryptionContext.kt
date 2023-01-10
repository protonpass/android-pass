package proton.android.pass.crypto.fakes.context

import proton.android.pass.crypto.api.context.EncryptionContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

class TestException(override val message: String) : IllegalStateException(message)

object TestEncryptionContext : EncryptionContext {
    private const val ENCRYPTED_SUFFIX = "-encrypted"
    private val ENCRYPTED_TRAIL = listOf(0xCA.toByte(), 0xFE.toByte())

    override fun encrypt(content: String): EncryptedString = "${content}$ENCRYPTED_SUFFIX"
    override fun encrypt(content: ByteArray): EncryptedByteArray {
        val cloned = content.clone().toMutableList()
        ENCRYPTED_TRAIL.forEach {
            cloned.add(it)
        }
        return EncryptedByteArray(cloned.toByteArray())
    }

    override fun decrypt(content: EncryptedString): String {
        if (!content.endsWith(ENCRYPTED_SUFFIX)) {
            throw TestException("Cannot decrypt. String does not contain the expected suffix")
        }
        return content.replace(ENCRYPTED_SUFFIX, "")
    }

    override fun decrypt(content: EncryptedByteArray): ByteArray {
        if (content.array.size < ENCRYPTED_TRAIL.size) {
            throw TestException("Cannot decrypt. Array does not end with the expected trail")
        }
        val startTrailIndex = content.array.size - ENCRYPTED_TRAIL.size
        ENCRYPTED_TRAIL.forEachIndexed { idx, byte ->
            val arrayIdx = idx + startTrailIndex
            if (content.array[arrayIdx] != byte) {
                throw TestException("Cannot decrypt. Array does not contain the expected trail byte at index $arrayIdx")
            }
        }

        val newByteArray = ByteArray(content.array.size - ENCRYPTED_TRAIL.size)
        for (i in 0 until content.array.size - ENCRYPTED_TRAIL.size) {
            newByteArray[i] = content.array[i]
        }
        return newByteArray
    }
}


package proton.android.pass.crypto.fakes.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionTag

class TestException(override val message: String) : IllegalStateException(message)

object TestEncryptionContext : EncryptionContext {
    private const val ENCRYPTED_SUFFIX = "-encrypted"
    private val ENCRYPTED_TRAIL = listOf(0xCA.toByte(), 0xFE.toByte())

    override fun encrypt(content: String): EncryptedString =
        Base64.encodeBase64String("${content}$ENCRYPTED_SUFFIX".encodeToByteArray())

    override fun encrypt(content: ByteArray, tag: EncryptionTag?): EncryptedByteArray {
        val cloned = content.clone().toMutableList()
        ENCRYPTED_TRAIL.forEach {
            cloned.add(it)
        }
        return EncryptedByteArray(cloned.toByteArray())
    }

    override fun decrypt(content: EncryptedString): String {
        val decoded = String(Base64.decodeBase64(content))
        if (!decoded.endsWith(ENCRYPTED_SUFFIX)) {
            throw TestException("Cannot decrypt. String does not contain the expected suffix")
        }
        return decoded.replace(ENCRYPTED_SUFFIX, "")
    }

    override fun decrypt(content: EncryptedByteArray, tag: EncryptionTag?): ByteArray =
        runCatching {
            decode(content.array)
        }.fold(
            onSuccess = { it },
            onFailure = { Base64.decodeBase64(content.array) }
        )

    private fun decode(byteArray: ByteArray): ByteArray {
        if (byteArray.size < ENCRYPTED_TRAIL.size) {
            throw TestException("Cannot decrypt. Array does not end with the expected trail")
        }
        val startTrailIndex = byteArray.size - ENCRYPTED_TRAIL.size
        ENCRYPTED_TRAIL.forEachIndexed { idx, byte ->
            val arrayIdx = idx + startTrailIndex
            if (byteArray[arrayIdx] != byte) {
                throw TestException("Cannot decrypt. Array does not contain the expected trail byte at index $arrayIdx")
            }
        }

        val newByteArray = ByteArray(byteArray.size - ENCRYPTED_TRAIL.size)
        for (i in 0 until byteArray.size - ENCRYPTED_TRAIL.size) {
            newByteArray[i] = byteArray[i]
        }
        return newByteArray
    }
}


package proton.android.pass.crypto.impl.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import org.apache.commons.codec.binary.Base64
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.error.BadTagException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionContextImpl(private val key: EncryptionKey) : EncryptionContext {
    override fun encrypt(content: String): EncryptedString {
        val encrypted = encrypt(content.encodeToByteArray())
        return Base64.encodeBase64String(encrypted.array)
    }

    override fun encrypt(content: ByteArray, tag: EncryptionTag?): EncryptedByteArray {
        val cipher = cipherFactory()
        val secretKey = SecretKeySpec(key.key, 0, key.key.size, algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        tag?.let { cipher.updateAAD(it.value) }
        val cipherByteArray = cipher.doFinal(content)
        return EncryptedByteArray(cipher.iv + cipherByteArray)
    }

    override fun decrypt(content: EncryptedString): String {
        val encryptedByteArray = Base64.decodeBase64(content)
        val decrypted = decrypt(EncryptedByteArray(encryptedByteArray))
        return decrypted.toString(Charsets.UTF_8)
    }

    override fun decrypt(content: EncryptedByteArray, tag: EncryptionTag?): ByteArray {
        val cipher = cipherFactory()
        val iv = content.array.copyOf(ivSize)
        val cipherByteArray = content.array.copyOfRange(ivSize, content.array.size)
        val secretKey = SecretKeySpec(key.key, 0, key.key.size, algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(cipherGCMTagBits, iv))
        tag?.let { cipher.updateAAD(it.value) }
        try {
            return cipher.doFinal(cipherByteArray)
        } catch (e: AEADBadTagException) {
            val tagName = tag?.name ?: "null"
            throw BadTagException("Bad AEAD Tag when decoding content [tag=$tagName]", e)
        }
    }

    companion object {
        private const val algorithm = "AES"
        private const val cipherTransformation = "AES/GCM/NoPadding"
        private const val ivSize = 12
        private const val cipherGCMTagBits = 128

        private fun cipherFactory(): Cipher = Cipher.getInstance(cipherTransformation)
    }
}

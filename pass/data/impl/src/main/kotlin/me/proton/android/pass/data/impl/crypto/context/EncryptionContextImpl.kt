package me.proton.android.pass.data.impl.crypto.context

import android.util.Base64
import me.proton.android.pass.data.api.crypto.EncryptionContext
import me.proton.android.pass.data.api.crypto.EncryptionKey
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionContextImpl(private val key: EncryptionKey) : EncryptionContext {
    override fun encrypt(content: String): EncryptedString {
        val encrypted = encrypt(content.encodeToByteArray())
        return Base64.encodeToString(encrypted.array, Base64.NO_WRAP)
    }

    override fun encrypt(content: ByteArray): EncryptedByteArray {
        val cipher = cipherFactory()
        val secretKey = SecretKeySpec(key.key, 0, key.key.size, algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val cipherByteArray = cipher.doFinal(content)
        return EncryptedByteArray(cipher.iv + cipherByteArray)
    }

    override fun decrypt(content: EncryptedString): String {
        val encryptedByteArray = Base64.decode(content, Base64.NO_WRAP)
        val decrypted = decrypt(EncryptedByteArray(encryptedByteArray))
        return decrypted.decodeToString()
    }

    override fun decrypt(content: EncryptedByteArray): ByteArray {
        val cipher = cipherFactory()
        val iv = content.array.copyOf(ivSize)
        val cipherByteArray = content.array.copyOfRange(ivSize, content.array.size)

        val secretKey = SecretKeySpec(key.key, 0, key.key.size, algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(cipherGCMTagBits, iv))
        return cipher.doFinal(cipherByteArray)
    }

    companion object {
        private const val algorithm = "AES"
        private const val cipherTransformation = "AES/GCM/NoPadding"
        private const val ivSize = 12
        private const val cipherGCMTagBits = 128

        private fun cipherFactory(): Cipher = Cipher.getInstance(cipherTransformation)
    }
}

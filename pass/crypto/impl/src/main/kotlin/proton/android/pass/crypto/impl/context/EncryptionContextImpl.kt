/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.crypto.impl.context

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.error.BadTagException
import java.lang.System.arraycopy
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionContextImpl(key: EncryptionKey) : EncryptionContext {

    private val secretKeySpec = SecretKeySpec(key.value(), ALGORITHM)

    override fun encrypt(content: String): EncryptedString {
        val encrypted = encrypt(content.encodeToByteArray())
        return Base64.encodeBase64String(encrypted.array)
    }

    override fun encrypt(content: ByteArray, tag: EncryptionTag?): EncryptedByteArray {
        val cipher = cipherFactory()
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)

        tag?.let { cipher.updateAAD(it.value) }

        val cipherByteArray = cipher.doFinal(content)
        val result = ByteArray(IV_SIZE + cipherByteArray.size)
        arraycopy(cipher.iv, 0, result, 0, IV_SIZE)
        arraycopy(cipherByteArray, 0, result, IV_SIZE, cipherByteArray.size)
        return EncryptedByteArray(result)
    }

    override fun decrypt(content: EncryptedString): String {
        val encryptedByteArray = Base64.decodeBase64(content)
        val decrypted = decrypt(EncryptedByteArray(encryptedByteArray))
        return decrypted.toString(Charsets.UTF_8)
    }

    override fun decrypt(content: EncryptedByteArray, tag: EncryptionTag?): ByteArray {
        val cipher = cipherFactory()

        val iv = content.array.copyOfRange(0, IV_SIZE)
        val cipherByteArray = content.array.copyOfRange(IV_SIZE, content.array.size)

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, GCMParameterSpec(CIPHER_GCM_TAG_BITS, iv))

        tag?.let { cipher.updateAAD(it.value) }
        try {
            return cipher.doFinal(cipherByteArray)
        } catch (e: AEADBadTagException) {
            val tagName = tag?.name ?: "null"
            throw BadTagException("Bad AEAD Tag when decoding content [tag=$tagName]", e)
        }
    }

    companion object {
        private const val ALGORITHM = "AES"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12
        private const val CIPHER_GCM_TAG_BITS = 128

        private fun cipherFactory(): Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
    }
}

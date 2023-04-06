package proton.android.pass.crypto.api

import me.proton.core.crypto.common.keystore.EncryptedString

object Base64 {

    fun encodeBase64String(array: ByteArray): EncryptedString = String(encodeBase64(array))

    fun decodeBase64(content: EncryptedString): ByteArray = decodeBase64(content.toByteArray())

    fun encodeBase64(array: ByteArray): ByteArray =
        org.apache.commons.codec.binary.Base64.encodeBase64(array)

    fun decodeBase64(array: ByteArray): ByteArray =
        org.apache.commons.codec.binary.Base64.decodeBase64(array)
}

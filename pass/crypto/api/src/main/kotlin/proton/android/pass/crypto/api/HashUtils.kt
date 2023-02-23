package proton.android.pass.crypto.api

import java.security.MessageDigest

object HashUtils {
    fun sha256(input: String): String = sha256(input.encodeToByteArray())

    fun sha256(input: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input)
        return digest.fold("") { str, r -> str + "%02x".format(r) }
    }
}

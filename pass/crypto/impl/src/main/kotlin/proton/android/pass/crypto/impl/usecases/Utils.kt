package proton.android.pass.crypto.impl.usecases

import java.util.UUID

object Utils {
    private const val PASSPHRASE_LENGTH = 32

    fun generatePassphrase() = getRandomString(PASSPHRASE_LENGTH)

    fun generateUuid(): String = UUID.randomUUID().toString()

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}


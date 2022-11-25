package me.proton.pass.presentation.detail.login

import me.proton.core.crypto.common.keystore.EncryptedString

sealed class PasswordState(open val encrypted: EncryptedString) {
    data class Concealed(override val encrypted: EncryptedString) : PasswordState(encrypted)
    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : PasswordState(encrypted)
}

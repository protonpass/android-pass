package me.proton.core.pass.domain

import me.proton.core.crypto.common.keystore.EncryptedString

sealed class ItemType {
    companion object {} // Needed for being able to define static extension functions

    data class Login(
        val username: String,
        val password: EncryptedString,
        val websites: List<String>
    ) : ItemType()
    data class Note(val text: String) : ItemType()
    data class Alias(val aliasEmail: String) : ItemType()
}

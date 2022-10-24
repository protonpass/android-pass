package me.proton.pass.domain

import androidx.annotation.StringRes
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.pass.domain.R

sealed class ItemType {

    data class Login(
        val username: String,
        val password: EncryptedString,
        val websites: List<String>
    ) : ItemType()
    data class Note(val text: String) : ItemType()
    data class Alias(val aliasEmail: String) : ItemType()
    object Password : ItemType()

    @StringRes fun toStringRes(): Int = when (this) {
        is Login -> R.string.item_type_login
        is Note -> R.string.item_type_note
        is Password -> R.string.item_type_password
        is Alias -> R.string.item_type_alias
    }

    companion object // Needed for being able to define static extension functions
}

package proton.pass.domain

import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.pass.domain.entity.PackageInfo

const val ITEM_TYPE_UNKNOWN = -1
const val ITEM_TYPE_LOGIN = 0
const val ITEM_TYPE_ALIAS = 1
const val ITEM_TYPE_NOTE = 2
const val ITEM_TYPE_PASSWORD = 3

@Serializable
sealed interface CustomField {
    val label: String

    @Serializable
    data class Text(override val label: String, val value: String) : CustomField

    @Serializable
    data class Hidden(override val label: String, val value: EncryptedString) : CustomField

    @Serializable
    data class Totp(override val label: String, val value: EncryptedString) : CustomField

    object Unknown : CustomField {
        override val label: String = "UNKNOWN"
    }
}

@Serializable
sealed interface ItemType {

    @Serializable
    data class Login(
        val username: String,
        val password: EncryptedString,
        val websites: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: EncryptedString,
        val customFields: List<CustomField>
    ) : ItemType

    @Serializable
    data class Note(val text: String) : ItemType

    @Serializable
    data class Alias(val aliasEmail: String) : ItemType

    @Serializable
    object Password : ItemType

    @Serializable
    object Unknown : ItemType

    @Suppress("MagicNumber")
    fun toWeightedInt(): Int = when (this) {
        is Login -> ITEM_TYPE_LOGIN
        is Alias -> ITEM_TYPE_ALIAS
        is Note -> ITEM_TYPE_NOTE
        is Password -> ITEM_TYPE_PASSWORD
        is Unknown -> ITEM_TYPE_UNKNOWN
    }

    companion object // Needed for being able to define static extension functions
}

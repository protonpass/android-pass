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
sealed interface ItemType {

    @Serializable
    data class Login(
        val username: String,
        val password: EncryptedString,
        val websites: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: EncryptedString,
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

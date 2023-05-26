package proton.pass.domain

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.pass.domain.entity.PackageInfo

sealed interface CustomFieldContent {
    val label: String

    data class Text(override val label: String, val value: String) : CustomFieldContent
    data class Hidden(override val label: String, val value: HiddenState) : CustomFieldContent
    data class Totp(override val label: String, val value: HiddenState) : CustomFieldContent
}

sealed class HiddenState(open val encrypted: EncryptedString) {
    data class Concealed(override val encrypted: EncryptedString) : HiddenState(encrypted)

    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : HiddenState(encrypted)
}

sealed class ItemContents(open val title: String, open val note: String) {
    data class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: String,
        val urls: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: String,
        val customFields: List<CustomFieldContent>
    ) : ItemContents(title, note)

    data class Note(
        override val title: String,
        override val note: String
    ) : ItemContents(title, note)

    data class Alias(
        override val title: String,
        override val note: String
    ) : ItemContents(title, note)
}

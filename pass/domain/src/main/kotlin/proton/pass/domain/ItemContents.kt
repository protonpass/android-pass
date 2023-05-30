package proton.pass.domain

import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.pass.domain.entity.PackageInfo

sealed interface CustomFieldContent {
    val label: String

    data class Text(override val label: String, val value: String) : CustomFieldContent
    data class Hidden(override val label: String, val value: HiddenState) : CustomFieldContent
    data class Totp(override val label: String, val value: HiddenState) : CustomFieldContent
}

@Serializable
sealed class HiddenState {
    abstract val encrypted: EncryptedString

    @Serializable
    data class Concealed(override val encrypted: EncryptedString) : HiddenState()

    @Serializable
    data class Revealed(
        override val encrypted: EncryptedString,
        val clearText: String
    ) : HiddenState()
}

@Serializable
sealed class ItemContents {
    abstract val title: String
    abstract val note: String

    @Serializable
    data class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: HiddenState,
        val urls: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: HiddenState,
        val customFields: List<CustomFieldContent>
    ) : ItemContents() {
        companion object {
            val Empty = Login(
                title = "",
                username = "",
                password = HiddenState.Concealed(""),
                urls = listOf(""),
                packageInfoSet = emptySet(),
                primaryTotp = HiddenState.Concealed(""),
                note = "",
                customFields = emptyList()
            )
        }
    }

    @Serializable
    data class Note(
        override val title: String,
        override val note: String
    ) : ItemContents()

    @Serializable
    data class Alias(
        override val title: String,
        override val note: String,
        val aliasEmail: String
    ) : ItemContents()

    @Serializable
    data class Unknown(
        override val title: String,
        override val note: String
    ) : ItemContents()

}

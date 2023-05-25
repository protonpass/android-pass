package proton.pass.domain

import proton.pass.domain.entity.PackageInfo

sealed interface CustomFieldContent {
    val label: String

    data class Text(override val label: String, val value: String) : CustomFieldContent
    data class Hidden(override val label: String, val value: String) : CustomFieldContent
    data class Totp(override val label: String, val value: String) : CustomFieldContent
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

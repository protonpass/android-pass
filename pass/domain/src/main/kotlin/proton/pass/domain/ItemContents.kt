package proton.pass.domain

import proton.pass.domain.entity.PackageInfo

sealed class ItemContents(open val title: String, open val note: String) {
    data class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: String,
        val urls: List<String>,
        val packageInfoSet: Set<PackageInfo>,
        val primaryTotp: String,
        val extraTotpSet: Set<String>
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

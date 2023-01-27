package proton.pass.domain

sealed class ItemContents(open val title: String, open val note: String) {
    data class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: String,
        val urls: List<String>,
        val packageNames: Set<String>,
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

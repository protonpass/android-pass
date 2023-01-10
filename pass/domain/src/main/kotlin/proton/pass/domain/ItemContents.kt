package proton.pass.domain

sealed class ItemContents(open val title: String, open val note: String) {
    class Login(
        override val title: String,
        override val note: String,
        val username: String,
        val password: String,
        val urls: List<String>
    ) : ItemContents(title, note)

    class Note(
        override val title: String,
        override val note: String
    ) : ItemContents(title, note)

    class Alias(
        override val title: String,
        override val note: String
    ) : ItemContents(title, note)
}

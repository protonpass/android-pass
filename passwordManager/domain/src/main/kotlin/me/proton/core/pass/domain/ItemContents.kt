package me.proton.core.pass.domain

sealed class ItemContents(open val title: String) {
    class Login(
        override val title: String,
        val username: String,
        val password: String
    ) : ItemContents(title)

    class Note(override val title: String, val text: String) : ItemContents(title)
}

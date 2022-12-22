package me.proton.pass.autofill.entities

sealed class AutofillItem {
    data class Login(
        val username: String,
        val password: String
    ) : AutofillItem()

    object Unknown : AutofillItem()
}

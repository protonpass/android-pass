package me.proton.android.pass.ui.create.login

import androidx.compose.runtime.Immutable
import me.proton.core.pass.domain.ItemContents

@Immutable
data class LoginItem(
    val title: String,
    val username: String,
    val password: String,
    val websiteAddresses: List<String>,
    val note: String
) {

    fun validate(): List<LoginItemValidationErrors> {
        val mutableList = mutableListOf<LoginItemValidationErrors>()
        if (title.isBlank()) mutableList.add(LoginItemValidationErrors.BlankTitle)
        return mutableList.toList()
    }

    fun toItemContents(): ItemContents {
        val addresses = websiteAddresses.filter { it.isNotEmpty() }
        return ItemContents.Login(
            title = title,
            note = note,
            username = username,
            password = password,
            urls = addresses
        )
    }

    companion object {
        val Empty = LoginItem(
            title = "",
            username = "",
            password = "",
            websiteAddresses = listOf(""),
            note = ""
        )
    }
}

sealed interface LoginItemValidationErrors {
    object BlankTitle : LoginItemValidationErrors
}

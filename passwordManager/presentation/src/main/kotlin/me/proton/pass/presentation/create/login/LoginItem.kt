package me.proton.pass.presentation.create.login

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ItemContents
import me.proton.pass.presentation.UrlSanitizer

@Immutable
data class LoginItem(
    val title: String,
    val username: String,
    val password: String,
    val websiteAddresses: List<String>,
    val note: String
) {

    fun validate(): Set<LoginItemValidationErrors> {
        val mutableSet = mutableSetOf<LoginItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(LoginItemValidationErrors.BlankTitle)
        websiteAddresses.forEachIndexed { idx, url ->
            if (url.isNotBlank()) {
                val validation = UrlSanitizer.sanitize(url)
                if (validation is Result.Error) {
                    mutableSet.add(LoginItemValidationErrors.InvalidUrl(idx))
                }
            }
        }

        return mutableSet.toSet()
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
    data class InvalidUrl(val index: Int) : LoginItemValidationErrors
}

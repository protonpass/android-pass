package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.url.UrlSanitizer
import proton.pass.domain.ItemContents

@Immutable
data class LoginItem(
    val title: String,
    val username: String,
    val password: String,
    val websiteAddresses: List<String>,
    val packageNames: Set<String>,
    val primaryTotp: String,
    val extraTotpSet: Set<String>,
    val note: String
) {

    fun validate(): Set<LoginItemValidationErrors> {
        val mutableSet = mutableSetOf<LoginItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(LoginItemValidationErrors.BlankTitle)
        websiteAddresses.forEachIndexed { idx, url ->
            if (url.isNotBlank()) {
                val validation = UrlSanitizer.sanitize(url)
                if (validation is LoadingResult.Error) {
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
            urls = addresses,
            packageNames = packageNames,
            primaryTotp = primaryTotp,
            extraTotpSet = extraTotpSet,
        )
    }

    companion object {
        val Empty = LoginItem(
            title = "",
            username = "",
            password = "",
            websiteAddresses = persistentListOf(""),
            packageNames = emptySet(),
            primaryTotp = "",
            extraTotpSet = emptySet(),
            note = ""
        )
    }
}

sealed interface LoginItemValidationErrors {
    object BlankTitle : LoginItemValidationErrors
    data class InvalidUrl(val index: Int) : LoginItemValidationErrors
}

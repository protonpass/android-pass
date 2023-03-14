package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.data.api.url.UrlSanitizer
import proton.pass.domain.ItemContents

@Immutable
data class LoginItem(
    val title: String,
    val username: String,
    val password: String,
    val websiteAddresses: List<String>,
    val packageInfoSet: ImmutableSet<PackageInfoUi>,
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
                if (validation.isFailure) {
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
            packageInfoSet = packageInfoSet.map(PackageInfoUi::toPackageInfo).toSet(),
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
            packageInfoSet = persistentSetOf(),
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

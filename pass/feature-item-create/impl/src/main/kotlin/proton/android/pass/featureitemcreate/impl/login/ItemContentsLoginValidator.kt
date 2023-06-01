package proton.android.pass.featureitemcreate.impl.login

import proton.android.pass.data.api.url.UrlSanitizer
import proton.pass.domain.ItemContents

fun ItemContents.Login.validate(): Set<LoginItemValidationErrors> {
    val mutableSet = mutableSetOf<LoginItemValidationErrors>()
    if (title.isBlank()) mutableSet.add(LoginItemValidationErrors.BlankTitle)
    urls.forEachIndexed { idx, url ->
        if (url.isNotBlank()) {
            val validation = UrlSanitizer.sanitize(url)
            if (validation.isFailure) {
                mutableSet.add(LoginItemValidationErrors.InvalidUrl(idx))
            }
        }
    }

    return mutableSet.toSet()
}

sealed interface LoginItemValidationErrors {
    object BlankTitle : LoginItemValidationErrors
    data class InvalidUrl(val index: Int) : LoginItemValidationErrors
    object InvalidTotp : LoginItemValidationErrors

    sealed interface CustomFieldValidationError : LoginItemValidationErrors {
        data class EmptyField(val index: Int) : CustomFieldValidationError
        data class InvalidTotp(val index: Int) : CustomFieldValidationError
    }
}

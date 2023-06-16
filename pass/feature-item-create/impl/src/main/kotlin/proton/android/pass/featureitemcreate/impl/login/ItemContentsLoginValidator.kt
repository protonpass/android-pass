/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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

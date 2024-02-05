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

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.domain.ItemContents
import proton.android.pass.featureitemcreate.impl.common.UICustomFieldContent
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState

@Parcelize
@Immutable
data class LoginItemFormState(
    val title: String,
    val note: String,
    val username: String,
    val password: UIHiddenState,
    val passwordStrength: PasswordStrength,
    val urls: List<String>,
    val packageInfoSet: Set<PackageInfoUi>,
    val primaryTotp: UIHiddenState,
    val customFields: List<UICustomFieldContent>,
    val passkeys: List<UIPasskeyContent>
) : Parcelable {

    fun validate(): Set<LoginItemValidationErrors> {
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

    fun toItemContents(): ItemContents.Login = ItemContents.Login(
        title = title,
        note = note,
        username = username,
        password = password.toHiddenState(),
        urls = urls,
        packageInfoSet = packageInfoSet.map(PackageInfoUi::toPackageInfo).toSet(),
        primaryTotp = primaryTotp.toHiddenState(),
        customFields = customFields.map(UICustomFieldContent::toCustomFieldContent),
        passkeys = passkeys.map(UIPasskeyContent::toDomain)
    )

    fun compare(other: LoginItemFormState, encryptionContext: EncryptionContext): Boolean =
        title == other.title &&
            note == other.note &&
            username == other.username &&
            encryptionContext.decrypt(password.encrypted.toEncryptedByteArray())
                .contentEquals(encryptionContext.decrypt(other.password.encrypted.toEncryptedByteArray())) &&
            urls == other.urls &&
            packageInfoSet == other.packageInfoSet &&
            encryptionContext.decrypt(primaryTotp.encrypted.toEncryptedByteArray())
                .contentEquals(encryptionContext.decrypt(other.primaryTotp.encrypted.toEncryptedByteArray())) &&
            customFields.size == other.customFields.size &&
            customFields.zip(other.customFields).all { (a, b) -> a.compare(b, encryptionContext) }

    companion object {

        fun default(encryptionContext: EncryptionContext) = LoginItemFormState(
            title = "",
            note = "",
            username = "",
            password = UIHiddenState.Empty(encryptionContext.encrypt("")),
            passwordStrength = PasswordStrength.None,
            urls = listOf(""),
            primaryTotp = UIHiddenState.Empty(encryptionContext.encrypt("")),
            packageInfoSet = emptySet(),
            customFields = emptyList(),
            passkeys = emptyList()
        )

    }

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

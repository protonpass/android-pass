/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.itemcreate.login

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIAutofillUrl
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState

@Parcelize
@Immutable
data class LoginItemFormState(
    val title: String,
    val note: String,
    val email: String,
    val username: String,
    val password: UIHiddenState,
    val passwordStrength: PasswordStrength,
    val urls: List<String>,
    val packageInfoSet: Set<PackageInfoUi>,
    val primaryTotp: UIHiddenState,
    val customFields: List<UICustomFieldContent>,
    val passkeys: List<UIPasskeyContent>,
    val autofillUrls: List<UIAutofillUrl>,
    val passkeyToBeGenerated: UIPasskeyContent?,
    private val isExpandedByContent: Boolean,
    private val isExpandedByUser: Boolean,
    private val isExpandedByPreference: Boolean
) : Parcelable {

    @IgnoredOnParcel
    internal val hasPasskeys: Boolean = passkeys.isNotEmpty()

    @IgnoredOnParcel
    internal val isExpanded: Boolean =
        isExpandedByContent || isExpandedByUser || isExpandedByPreference

    internal fun toItemContents(emailValidator: EmailValidator): ItemContents.Login {
        val itemEmail = when {
            email.isNotBlank() && username.isNotBlank() -> email
            email.isNotBlank() -> if (emailValidator.isValid(email)) email else ""
            else -> if (emailValidator.isValid(username)) username else ""
        }

        val itemUsername = when {
            username.isNotBlank() && email.isNotBlank() -> username
            username.isNotBlank() -> if (emailValidator.isValid(username)) "" else username
            else -> if (emailValidator.isValid(email)) "" else email
        }

        val nonBlankUrls = urls.filter(String::isNotBlank)
        val syncedAutofillUrls: List<AutofillUrl> = nonBlankUrls.mapIndexed { idx, url ->
            val mode = autofillUrls.getOrNull(idx)?.mode ?: AutofillUrlMode.Default
            AutofillUrl(url = url, mode = mode)
        }
        return ItemContents.Login(
            title = title,
            note = note,
            itemEmail = itemEmail,
            itemUsername = itemUsername,
            password = password.toHiddenState(),
            urls = syncedAutofillUrls
                .filter { it.mode == AutofillUrlMode.Default }
                .map { it.url }
                .distinct(),
            packageInfoSet = packageInfoSet.map(PackageInfoUi::toPackageInfo).toSet(),
            primaryTotp = primaryTotp.toHiddenState(),
            customFields = customFields.map(UICustomFieldContent::toCustomFieldContent),
            passkeys = if (passkeys.isEmpty()) {
                passkeyToBeGenerated?.toDomain()?.let { listOf(it) } ?: emptyList()
            } else {
                passkeys.map(UIPasskeyContent::toDomain)
            },
            autofillUrls = syncedAutofillUrls
        )
    }

    internal fun compare(other: LoginItemFormState, encryptionContext: EncryptionContext): Boolean =
        title == other.title &&
            note == other.note &&
            email == other.email &&
            username == other.username &&
            encryptionContext.decrypt(password.encrypted.toEncryptedByteArray())
                .contentEquals(encryptionContext.decrypt(other.password.encrypted.toEncryptedByteArray())) &&
            urls == other.urls &&
            packageInfoSet == other.packageInfoSet &&
            encryptionContext.decrypt(primaryTotp.encrypted.toEncryptedByteArray())
                .contentEquals(encryptionContext.decrypt(other.primaryTotp.encrypted.toEncryptedByteArray())) &&
            customFields.size == other.customFields.size &&
            customFields.zip(other.customFields)
                .all { (a, b) -> a.compare(b, encryptionContext) }

    internal companion object {

        internal fun default(encryptionContext: EncryptionContext) = LoginItemFormState(
            title = "",
            note = "",
            email = "",
            username = "",
            password = UIHiddenState.Empty(encryptionContext.encrypt("")),
            passwordStrength = PasswordStrength.None,
            urls = listOf(""),
            primaryTotp = UIHiddenState.Empty(encryptionContext.encrypt("")),
            packageInfoSet = emptySet(),
            customFields = emptyList(),
            passkeys = emptyList(),
            autofillUrls = emptyList(),
            passkeyToBeGenerated = null,
            isExpandedByContent = false,
            isExpandedByUser = false,
            isExpandedByPreference = false
        )

    }

}

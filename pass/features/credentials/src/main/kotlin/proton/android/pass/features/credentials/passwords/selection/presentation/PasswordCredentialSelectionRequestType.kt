/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.features.credentials.passwords.selection.presentation

import proton.android.pass.features.credentials.shared.passwords.domain.PasswordRequestType

internal enum class PasswordCredentialSelectionRequestType {
    Select,
    Use,
    Unknown;

    internal companion object {

        internal fun from(value: String?): PasswordCredentialSelectionRequestType = when (value) {
            PasswordRequestType.SelectPassword.name -> Select
            PasswordRequestType.UsePassword.name -> Use
            else -> Unknown
        }

    }
}

internal inline fun <T> PasswordCredentialSelectionRequestType.dispatch(
    onSelect: () -> T,
    onUse: () -> T,
    onUnknown: () -> T
): T = when (this) {
    PasswordCredentialSelectionRequestType.Select -> onSelect()
    PasswordCredentialSelectionRequestType.Use -> onUse()
    PasswordCredentialSelectionRequestType.Unknown -> onUnknown()
}

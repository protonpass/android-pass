/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passwords.selection.presentation

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.data.api.usecases.Suggestion

internal sealed interface PasswordCredentialSelectionRequest {

    val title: String

    val suggestion: Suggestion

    data class Select(
        override val title: String,
        override val suggestion: Suggestion
    ) : PasswordCredentialSelectionRequest

    data class Use(
        override val title: String,
        override val suggestion: Suggestion,
        internal val username: String,
        internal val encryptedPassword: EncryptedString
    ) : PasswordCredentialSelectionRequest

}

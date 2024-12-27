/*
 * Copyright (c) 2024 Proton AG
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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class EmailUsernameInputPreviewProvider :
    PreviewParameterProvider<EmailUsernameInputPreviewParams> {

    private val booleans = listOf(true, false)
    private val emails = listOf("", "user@email.com")
    private val usernames = listOf("", "username")

    override val values: Sequence<EmailUsernameInputPreviewParams> = sequence {
        for (boolean in booleans) {
            for (email in emails) {
                for (username in usernames) {
                    yield(
                        EmailUsernameInputPreviewParams(
                            email = email,
                            username = username,
                            isEditAllowed = boolean,
                            canUpdateUsername = boolean,
                            isExpanded = boolean
                        )
                    )
                }
            }
        }
    }
}

internal data class EmailUsernameInputPreviewParams(
    internal val email: String,
    internal val username: String,
    internal val isEditAllowed: Boolean,
    internal val canUpdateUsername: Boolean,
    internal val isExpanded: Boolean
)

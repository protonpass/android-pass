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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.features.credentials.shared.passwords.domain.PasswordRequestType

internal class PasswordCredentialSelectionRequestTypeTest {

    @Test
    internal fun `Use password request does not take the Begin request dispatch path`() {
        val requestType = PasswordCredentialSelectionRequestType.from(PasswordRequestType.UsePassword.name)
        var beginRequestWasRead = false

        requestType.dispatch(
            onSelect = {
                beginRequestWasRead = true
            },
            onUse = { },
            onUnknown = { }
        )

        assertThat(beginRequestWasRead).isFalse()
    }

    @Test
    internal fun `Select password request takes the Begin request dispatch path`() {
        val requestType = PasswordCredentialSelectionRequestType.from(PasswordRequestType.SelectPassword.name)
        var beginRequestWasRead = false

        requestType.dispatch(
            onSelect = {
                beginRequestWasRead = true
            },
            onUse = { },
            onUnknown = { }
        )

        assertThat(beginRequestWasRead).isTrue()
    }
}

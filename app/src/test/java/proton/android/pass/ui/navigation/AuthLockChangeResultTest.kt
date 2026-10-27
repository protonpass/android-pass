/*
 * Copyright (c) 2026 Proton AG
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
package proton.android.pass.ui.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.features.auth.AuthNavigation
import proton.android.pass.features.auth.AuthOrigin
import proton.android.pass.features.profile.MASTER_PASSWORD_VERIFIED_KEY

class AuthLockChangeResultTest {

    @Test
    fun `successful master password check for a lock change sets the verified flag`() {
        val result = AuthNavigation.Success(AuthOrigin.CHANGE_APP_LOCK_TYPE).lockChangeResult()

        assertThat(result).isEqualTo(NavigationResult(MASTER_PASSWORD_VERIFIED_KEY, true))
    }

    @Test
    fun `closing the auth screen for a lock change leaves the verified flag unset`() {
        val result = AuthNavigation.CloseScreen(AuthOrigin.CHANGE_APP_LOCK_TYPE).lockChangeResult()

        assertThat(result).isNull()
    }

    @Test
    fun `failed and dismissed auth leave the verified flag unset`() {
        assertThat(AuthNavigation.Failed.lockChangeResult()).isNull()
        assertThat(AuthNavigation.Dismissed.lockChangeResult()).isNull()
    }

    @Test
    fun `success for other origins never sets the verified flag`() {
        AuthOrigin.entries
            .filter { it != AuthOrigin.CHANGE_APP_LOCK_TYPE }
            .forEach { origin ->
                assertThat(AuthNavigation.Success(origin).lockChangeResult()).isNull()
            }
    }
}

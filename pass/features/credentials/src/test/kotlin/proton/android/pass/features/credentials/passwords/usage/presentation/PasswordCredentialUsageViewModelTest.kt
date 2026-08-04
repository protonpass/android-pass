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

package proton.android.pass.features.credentials.passwords.usage.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeHasActiveAccount
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.MainDispatcherRule

internal class PasswordCredentialUsageViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    @Test
    internal fun `WHEN no active account for userId THEN stateFlow emits Cancel`() = runTest {
        val hasActiveAccount = FakeHasActiveAccount()
        hasActiveAccount.setResult(false)

        val viewModel = PasswordCredentialUsageViewModel(
            encryptionContextProvider = FakeEncryptionContextProvider(),
            hasActiveAccount = hasActiveAccount,
            userPreferenceRepository = FakePreferenceRepository()
        )

        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(PasswordCredentialUsageState.NotReady)

            viewModel.onUpdateRequest(
                PasswordCredentialUsageRequest(
                    userId = UserId("user-id"),
                    username = "alice",
                    encryptedPassword = FakeEncryptionContext.encrypt("s3cret")
                )
            )

            assertThat(awaitItem()).isEqualTo(PasswordCredentialUsageState.Cancel)
        }
    }

}

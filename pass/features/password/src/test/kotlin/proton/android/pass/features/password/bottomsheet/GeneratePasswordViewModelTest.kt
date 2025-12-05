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

package proton.android.pass.features.password.bottomsheet

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.fakes.FakePasswordGenerator
import proton.android.pass.commonrust.fakes.passwords.strengths.FakePasswordStrengthCalculator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeAddOnePasswordHistoryEntryToUser
import proton.android.pass.data.fakes.usecases.passwords.FakeObservePasswordConfig
import proton.android.pass.data.fakes.usecases.passwords.FakeUpdatePasswordConfig
import proton.android.pass.features.password.GeneratePasswordBottomsheetMode
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule

internal class GeneratePasswordViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var stateHandleProvider: SavedStateHandleProvider
    private lateinit var passwordStrengthCalculator: FakePasswordStrengthCalculator
    private lateinit var viewModel: GeneratePasswordViewModel

    @Before
    internal fun setUp() {
        stateHandleProvider = FakeSavedStateHandleProvider()
        stateHandleProvider.get()[GeneratePasswordBottomsheetMode.key] =
            GeneratePasswordBottomsheetModeValue.CancelConfirm.name

        passwordStrengthCalculator = FakePasswordStrengthCalculator()

        viewModel = GeneratePasswordViewModel(
            stateHandleProvider = stateHandleProvider,
            passwordStrengthCalculator = passwordStrengthCalculator,
            snackbarDispatcher = FakeSnackbarDispatcher(),
            clipboardManager = FakeClipboardManager(),
            draftRepository = FakeDraftRepository(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            passwordGenerator = FakePasswordGenerator(),
            observePasswordConfig = FakeObservePasswordConfig(),
            updatePasswordConfig = FakeUpdatePasswordConfig(),
            addOnePasswordHistoryEntryToUser = FakeAddOnePasswordHistoryEntryToUser(),
            clock = FixedClock()
        )
    }

    @Test
    internal fun `WHEN view model is initialized THEN password strength is None`() = runTest {
        val expectedPasswordStrength = PasswordStrength.None

        viewModel.stateFlow.test {
            assertThat(awaitItem().passwordStrength).isEqualTo(expectedPasswordStrength)
        }
    }

    @Test
    internal fun `WHEN a new password is generated THEN password strength is updated`() = runTest {
        val expectedPasswordStrength = PasswordStrength.Strong
        passwordStrengthCalculator.setPasswordStrength(expectedPasswordStrength)

        viewModel.onRegeneratePassword()

        viewModel.stateFlow.test {
            assertThat(awaitItem().passwordStrength).isEqualTo(expectedPasswordStrength)
        }
    }

}

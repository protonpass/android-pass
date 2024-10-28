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

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.fakes.FakePasswordGenerator
import proton.android.pass.commonrust.fakes.passwords.strengths.TestPasswordStrengthCalculator
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.features.password.GeneratePasswordBottomsheetMode
import proton.android.pass.features.password.GeneratePasswordBottomsheetModeValue
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule

internal class GeneratePasswordViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var passwordStrengthCalculator: TestPasswordStrengthCalculator
    private lateinit var viewModel: GeneratePasswordViewModel

    @Before
    internal fun setUp() {
        savedStateHandle = TestSavedStateHandleProvider().get()
        savedStateHandle[GeneratePasswordBottomsheetMode.key] =
            GeneratePasswordBottomsheetModeValue.CancelConfirm.name

        passwordStrengthCalculator = TestPasswordStrengthCalculator()

        viewModel = GeneratePasswordViewModel(
            savedStateHandle = savedStateHandle,
            userPreferencesRepository = TestPreferenceRepository(),
            passwordStrengthCalculator = passwordStrengthCalculator,
            snackbarDispatcher = TestSnackbarDispatcher(),
            clipboardManager = TestClipboardManager(),
            draftRepository = TestDraftRepository(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            passwordGenerator = FakePasswordGenerator()
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

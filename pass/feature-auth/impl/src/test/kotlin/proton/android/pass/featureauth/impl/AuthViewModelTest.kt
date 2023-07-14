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

package proton.android.pass.featureauth.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStartupError
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.biometry.TestStoreAuthSuccessful
import proton.android.pass.common.api.None
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.usecases.TestCheckMasterPassword
import proton.android.pass.data.fakes.usecases.TestObservePrimaryUserEmail
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule

class AuthViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager
    private lateinit var checkMasterPassword: TestCheckMasterPassword

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        biometryManager = TestBiometryManager()
        checkMasterPassword = TestCheckMasterPassword()
        viewModel = AuthViewModel(
            preferenceRepository = preferenceRepository,
            biometryManager = biometryManager,
            checkMasterPassword = checkMasterPassword,
            storeAuthSuccessful = TestStoreAuthSuccessful(),
            internalSettingsRepository = TestInternalSettingsRepository(),
            observePrimaryUserEmail = TestObservePrimaryUserEmail().apply {
                emit(USER_EMAIL)
            }
        )
    }

    @Test
    fun `sends correct initial state`() = runTest {
        viewModel.state.test {
            val expected = AuthState.Initial.copy(
                event = AuthEvent.Unknown,
                content = AuthContent.default(USER_EMAIL)
            )
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `biometry success with enabled biometry emits success state`() = runTest {
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Success)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Success)
        }
    }

    @Test
    fun `biometry preference is respected`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        preferenceRepository.setAppLockState(AppLockState.Disabled)

        // Set to failed so we can check it is not called
        biometryManager.emitResult(BiometryResult.Failed)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Success)
        }

        assertThat(biometryManager.hasBeenCalled).isFalse()
    }

    @Test
    fun `unavailable biometry leads to auth success`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Success)
        }
    }

    @Test
    fun `not enrolled biometry leads to auth success`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.setBiometryStatus(BiometryStatus.NotEnrolled)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Success)
        }
    }

    @Test
    fun `biometry error cancel emits initial state`() = runTest {
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(
                AuthState(
                    event = AuthEvent.Unknown,
                    content = AuthContent.default(USER_EMAIL)
                )
            )
        }
    }


    @Test
    fun `biometry error of any other kind emits failed`() = runTest {
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Unknown))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Failed)
        }
    }

    @Test
    fun `biometry error failed to start emits failed`() = runTest {
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.FailedToStart(BiometryStartupError.Unknown))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Failed)
        }
    }

    @Test
    fun `click on sign out emits logout`() = runTest {
        viewModel.onSignOut()
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.SignOut)
        }
    }

    @Test
    fun `correct password emits success`() = runTest {
        viewModel.onPasswordChanged("password")
        viewModel.onSubmit()
        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(AuthEvent.Success)
        }
    }

    @Test
    fun `empty password emits password error`() = runTest {
        setBiometryCanceled()

        viewModel.onPasswordChanged("")
        viewModel.onSubmit()
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.content.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(state.content.passwordError.value()).isEqualTo(PasswordError.EmptyPassword)
        }
    }

    @Test
    fun `wrong password emits wrong password error`() = runTest {
        setBiometryCanceled()
        checkMasterPassword.setResult(false)

        val password = "test"

        viewModel.onPasswordChanged(password)
        viewModel.onSubmit()
        viewModel.state.test {
            val state1 = awaitItem()
            assertThat(state1.content.isLoadingState).isEqualTo(IsLoadingState.Loading)

            val state2 = awaitItem()
            assertThat(state2.content.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(state2.content.error.value()).isEqualTo(AuthError.WrongPassword(remainingAttempts = 4))
            assertThat(state2.content.password).isEqualTo(password)
        }
    }

    @Test
    fun `wrong password many times emits logout`() = runTest {
        setBiometryCanceled()
        checkMasterPassword.setResult(false)

        val password = "test"

        viewModel.onPasswordChanged(password)

        viewModel.state.test {
            skipItems(1)

            // Fail 4 times
            for (attempt in 0 until 4) {
                viewModel.onSubmit()
                val stateLoading = awaitItem()
                assertThat(stateLoading.content.isLoadingState).isEqualTo(IsLoadingState.Loading)

                val stateError = awaitItem()
                assertThat(stateError.content.isLoadingState).isEqualTo(IsLoadingState.NotLoading)

                val expected = AuthError.WrongPassword(remainingAttempts = 4 - attempt)
                assertThat(stateError.content.error.value()).isEqualTo(expected)
            }

            // Fail one more time
            viewModel.onSubmit()
            val stateLoading = awaitItem()
            assertThat(stateLoading.content.isLoadingState).isEqualTo(IsLoadingState.Loading)

            val stateError = awaitItem()
            assertThat(stateError.content.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(stateError.event).isEqualTo(AuthEvent.ForceSignOut)
        }
    }

    private suspend fun setBiometryCanceled() {
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))
        viewModel.init(ContextHolder(None))
    }

    companion object {
        private const val USER_EMAIL = "test@test.test"
    }
}

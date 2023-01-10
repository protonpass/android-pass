package proton.android.pass.featureauth.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import proton.android.pass.common.api.None
import proton.android.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStartupError
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.ContextHolder
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.TestPreferenceRepository

class AuthViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        biometryManager = TestBiometryManager()
        viewModel = AuthViewModel(preferenceRepository, biometryManager)
    }

    @Test
    fun `sends correct initial state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Pending)
        }
    }

    @Test
    fun `biometry success with enabled biometry emits success state`() = runTest {
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Success)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Success)
        }
    }

    @Test
    fun `biometry preference is respected`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        preferenceRepository.setBiometricLockState(BiometricLockState.Disabled)

        // Set to failed so we can check it is not called
        biometryManager.emitResult(BiometryResult.Failed)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Success)
        }
    }

    @Test
    fun `unavailable biometry leads to auth success`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Success)
        }
    }

    @Test
    fun `not enrolled biometry leads to auth success`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.setBiometryStatus(BiometryStatus.NotEnrolled)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Success)
        }
    }

    @Test
    fun `biometry error cancel emits cancelled`() = runTest {
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Canceled)
        }
    }

    @Test
    fun `biometry error user cancel emits cancelled`() = runTest {
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.UserCanceled))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Canceled)
        }
    }

    @Test
    fun `biometry error of any other kind emits failed`() = runTest {
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Unknown))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Failed)
        }
    }

    @Test
    fun `biometry error failed to start emits failed`() = runTest {
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.FailedToStart(BiometryStartupError.Unknown))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Failed)
        }
    }
}

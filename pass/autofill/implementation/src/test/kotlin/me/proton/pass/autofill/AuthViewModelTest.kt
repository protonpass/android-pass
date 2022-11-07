package me.proton.pass.autofill

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStartupError
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.biometry.TestBiometryManager
import me.proton.pass.presentation.auth.AuthStatus
import me.proton.pass.presentation.auth.AuthViewModel
import me.proton.pass.common.api.None
import me.proton.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var biometryManager: TestBiometryManager

    @Before
    fun setUp() {
        biometryManager = TestBiometryManager()
        viewModel = AuthViewModel(biometryManager)
    }

    @Test
    fun `sends correct initial state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Pending)
        }
    }

    @Test
    fun `biometry success emits success state`() = runTest {
        biometryManager.emitResult(BiometryResult.Success)

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Success)
        }
    }

    @Test
    fun `biometry error cancel emits cancelled`() = runTest {
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Canceled)
        }
    }

    @Test
    fun `biometry error user cancel emits cancelled`() = runTest {
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.UserCanceled))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Canceled)
        }
    }

    @Test
    fun `biometry error of any other kind emits failed`() = runTest {
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Unknown))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Failed)
        }
    }

    @Test
    fun `biometry error failed to start emits failed`() = runTest {
        biometryManager.emitResult(BiometryResult.FailedToStart(BiometryStartupError.Unknown))

        viewModel.init(ContextHolder(None))
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AuthStatus.Failed)
        }
    }
}
